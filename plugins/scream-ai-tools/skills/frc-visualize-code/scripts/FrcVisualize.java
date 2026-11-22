// SCREAM AI Tools — frc-visualize-code
// SPDX-License-Identifier: MIT
//
// Maps the structure of an FRC (WPILib) Java robot project, starting from Robot.java, and writes
// an interactive HTML page, an SVG class diagram and a JSON graph.
//
// Zero dependencies: it uses the JDK's own Java parser (the javac Compiler Tree API), so it runs
// offline on the JDK that the WPILib installer already ships. It parses source only — the project
// does not need to build. Run it with the source-file launcher (JDK 17 or newer):
//
//   java FrcVisualize.java [--project DIR] [--out DIR] [--root CLASS]
//
// Everything it reports is a static reading of source text. It never runs robot code.

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.tools.*;

public class FrcVisualize {

  static final String VERSION = "1.0.0";

  // ---------------------------------------------------------------------------------------------
  // Model
  // ---------------------------------------------------------------------------------------------

  static final class FieldInfo {
    String name;
    List<String> typeNames = new ArrayList<>();
    String typeText;
    boolean isStatic;
    String newType; // simple/qualified name when initialised with `new X(...)`
    ExpressionTree init;
    int line;
  }

  static final class MethodInfo {
    String name;
    String returnType;
    boolean isPublic;
    boolean isConstructor;
    MethodTree tree;
    int line;
    List<List<String>> paramTypeNames = new ArrayList<>();
  }

  static final class TypeInfo {
    String fqn, simple, pkg, kind, file;
    int line;
    TypeInfo outer;
    ClassTree tree;
    CompilationUnitTree cu;
    List<String> singleImports = new ArrayList<>();
    List<String> starImports = new ArrayList<>();
    List<String> extendsNames = new ArrayList<>();
    List<String> implementsNames = new ArrayList<>();
    Map<String, FieldInfo> fields = new LinkedHashMap<>();
    List<MethodInfo> methods = new ArrayList<>();
    Set<String> refs = new LinkedHashSet<>();
    Set<String> news = new LinkedHashSet<>();
    boolean project; // under the robot project's own source root (vs. vendored library source)
    String origin; // "project" or the top-level folder of the vendored library
    String category;
    boolean heuristicSubsystem;
  }

  record Edge(String src, String dst, String kind, Set<String> labels) {}

  /** Plain-language wording for each edge kind (the page script has the same words). */
  static final Map<String, String> EDGE_WORDS = Map.of(
      "extends", "extends", "implements", "implements",
      "owns", "creates and stores (field = new ...)", "has", "stores a reference to (field)",
      "injected", "is passed in its constructor", "creates", "creates", "uses", "refers to");

  static final List<String> EDGE_ORDER =
      List.of("extends", "implements", "owns", "has", "injected", "creates", "uses");

  // ---------------------------------------------------------------------------------------------
  // State
  // ---------------------------------------------------------------------------------------------

  final Path projectDir;
  final Map<String, TypeInfo> types = new LinkedHashMap<>();
  final Map<CompilationUnitTree, String> sources = new HashMap<>();
  SourcePositions positions;
  final List<String> warnings = new ArrayList<>();
  final Map<String, Integer> frameworkImports = new TreeMap<>();

  FrcVisualize(Path projectDir) {
    this.projectDir = projectDir;
  }

  // ---------------------------------------------------------------------------------------------
  // Entry point
  // ---------------------------------------------------------------------------------------------

  public static void main(String[] args) throws Exception {
    Path project = Paths.get(".");
    Path out = null;
    String rootName = null;
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--project" -> project = Paths.get(args[++i]);
        case "--out" -> out = Paths.get(args[++i]);
        case "--root" -> rootName = args[++i];
        case "--version" -> {
          System.out.println("frc-visualize-code " + VERSION);
          return;
        }
        case "-h", "--help" -> {
          System.out.println(
              "usage: java FrcVisualize.java [--project DIR] [--out DIR] [--root CLASS]\n"
                  + "  --project  robot project folder (default: current folder)\n"
                  + "  --out      output folder (default: <project>/docs/projVisu)\n"
                  + "  --root     entry class, simple or fully qualified (default: Robot)");
          return;
        }
        default -> {
          System.err.println("Unknown argument: " + args[i] + " (try --help)");
          System.exit(2);
        }
      }
    }
    project = project.toAbsolutePath().normalize();
    if (out == null) out = project.resolve("docs").resolve("projVisu");
    out = out.toAbsolutePath().normalize();
    System.exit(new FrcVisualize(project).run(out, rootName));
  }

  int run(Path outDir, String rootName) throws IOException {
    List<Path> files = findSources();
    if (files.isEmpty()) {
      System.err.println("No .java files found under " + projectDir);
      return 2;
    }
    if (!parse(files)) return 2;

    TypeInfo root = findRoot(rootName);
    if (root == null) {
      System.err.println(
          "Could not find the robot entry class. Looked for a class extending TimedRobot /"
              + " RobotBase (or named Robot). Pass --root <ClassName>.");
      return 3;
    }
    Path sourceRoot = sourceRootOf(root);
    for (TypeInfo t : types.values()) {
      Path f = projectDir.resolve(t.file);
      t.project = sourceRoot != null && f.startsWith(sourceRoot);
      if (t.project) t.origin = "project";
      else {
        Path rel = projectDir.relativize(f);
        t.origin = rel.getNameCount() > 1 ? rel.getName(0).toString() : "library";
      }
    }
    for (TypeInfo t : types.values()) t.category = categorize(t, root);

    // Reachability from the root over every reference kind.
    List<Edge> rawEdges = new ArrayList<>();
    Set<String> reachable = reach(root, rawEdges);

    Map<String, Object> bindings = extractBindings(reachable);

    Graph arch = buildView(root, reachable, rawEdges, false);
    Graph full = buildView(root, reachable, rawEdges, true);
    String archSvg = new Layout(arch, this, "arch").svg();
    String fullSvg = new Layout(full, this, "full").svg();

    Map<String, Object> meta = projectMeta(root);
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("tool", "frc-visualize-code " + VERSION);
    json.put("project", meta);
    json.put("root", root.fqn);
    json.put("classes", classesJson(reachable));
    json.put("architectureView", viewJson(arch));
    json.put("fullView", viewJson(full));
    json.putAll(bindings);
    json.put("warnings", warnings);

    Files.createDirectories(outDir);
    String jsonText = Json.write(json);
    Files.writeString(outDir.resolve("structure.json"), jsonText + "\n", StandardCharsets.UTF_8);
    Files.writeString(outDir.resolve("structure.svg"), archSvg, StandardCharsets.UTF_8);
    Files.writeString(
        outDir.resolve("structure-full.svg"), fullSvg, StandardCharsets.UTF_8);
    Files.writeString(
        outDir.resolve("index.html"),
        Html.page(meta, jsonText, archSvg, fullSvg),
        StandardCharsets.UTF_8);

    @SuppressWarnings("unchecked")
    List<Object> bl = (List<Object>) bindings.get("bindings");
    @SuppressWarnings("unchecked")
    List<Object> dl = (List<Object>) bindings.get("defaultCommands");
    System.out.println("frc-visualize-code " + VERSION);
    System.out.println("  project:      " + projectDir);
    System.out.println("  root:         " + root.fqn);
    System.out.println("  framework:    " + meta.get("commandFramework"));
    System.out.println("  WPILib:       " + meta.get("wpilibVersion"));
    System.out.println("  classes:      " + reachable.size() + " reachable from root ("
        + arch.nodes.size() + " in architecture view, " + full.nodes.size() + " in full view)");
    System.out.println("  bindings:     " + bl.size() + " button bindings, " + dl.size()
        + " default commands");
    System.out.println("  warnings:     " + warnings.size());
    for (String w : warnings) System.out.println("    - " + w);
    System.out.println("  wrote:        " + outDir.resolve("index.html"));
    System.out.println("                " + outDir.resolve("structure.svg"));
    System.out.println("                " + outDir.resolve("structure-full.svg"));
    System.out.println("                " + outDir.resolve("structure.json"));
    return 0;
  }

  // ---------------------------------------------------------------------------------------------
  // Source discovery and parsing
  // ---------------------------------------------------------------------------------------------

  static final Set<String> SKIP_DIRS =
      Set.of("build", "bin", "out", ".gradle", ".git", ".idea", ".vscode", "node_modules",
          ".wpilib", "gradle", "docs", "vendordeps", "test", "tests", "testFixtures", ".claude");

  List<Path> findSources() throws IOException {
    try (Stream<Path> s = Files.walk(projectDir)) {
      return s.filter(p -> p.toString().endsWith(".java"))
          .filter(p -> {
            Path rel = projectDir.relativize(p);
            for (int i = 0; i < rel.getNameCount() - 1; i++)
              if (SKIP_DIRS.contains(rel.getName(i).toString())) return false;
            return Files.isRegularFile(p);
          })
          .sorted()
          .collect(Collectors.toList());
    }
  }

  boolean parse(List<Path> files) throws IOException {
    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    if (jc == null) {
      System.err.println("This Java is a JRE, not a JDK: it has no Java parser. Use the JDK that"
          + " ships with WPILib (e.g. ~/wpilib/<year>/jdk).");
      return false;
    }
    DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
    StandardJavaFileManager fm = jc.getStandardFileManager(diags, null, StandardCharsets.UTF_8);
    JavacTask task = (JavacTask) jc.getTask(null, fm, diags, List.of("-proc:none"), null,
        fm.getJavaFileObjectsFromPaths(files));
    Iterable<? extends CompilationUnitTree> units = task.parse();
    positions = Trees.instance(task).getSourcePositions();
    Map<String, Integer> errorsPerFile = new TreeMap<>();
    for (Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics()) {
      if (d.getKind() == Diagnostic.Kind.ERROR && d.getSource() != null) {
        String f = projectDir.relativize(Paths.get(d.getSource().toUri())).toString();
        errorsPerFile.merge(f, 1, Integer::sum);
      }
    }
    errorsPerFile.forEach((f, n) -> warnings.add("Parse errors in " + f + " (" + n
        + "). Results for that file may be incomplete; this JDK is "
        + Runtime.version().feature() + " — newer Java syntax needs a newer JDK."));
    for (CompilationUnitTree cu : units) {
      sources.put(cu, cu.getSourceFile().getCharContent(true).toString());
      String pkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString();
      List<String> single = new ArrayList<>(), star = new ArrayList<>();
      for (ImportTree it : cu.getImports()) {
        if (it.isStatic()) continue;
        String q = it.getQualifiedIdentifier().toString();
        if (q.endsWith(".*")) star.add(q.substring(0, q.length() - 2));
        else single.add(q);
        countFramework(q);
      }
      String file = projectDir.relativize(Paths.get(cu.getSourceFile().toUri())).toString()
          .replace('\\', '/');
      for (Tree t : cu.getTypeDecls())
        if (t instanceof ClassTree ct) collectType(ct, cu, pkg, single, star, file, null);
    }
    return true;
  }

  void countFramework(String imp) {
    String key = null;
    if (imp.startsWith("edu.wpi.first.wpilibj2.command")) key = "v2 (edu.wpi.first.wpilibj2.command)";
    else if (imp.startsWith("org.wpilib.command2")) key = "v2 (org.wpilib.command2)";
    else if (imp.startsWith("org.wpilib.command3")) key = "v3 (org.wpilib.command3)";
    else if (imp.startsWith("edu.wpi.first.wpilibj.command")) key = "v1 legacy (edu.wpi.first.wpilibj.command)";
    if (key != null) frameworkImports.merge(key, 1, Integer::sum);
  }

  void collectType(ClassTree ct, CompilationUnitTree cu, String pkg, List<String> single,
      List<String> star, String file, TypeInfo outer) {
    TypeInfo t = new TypeInfo();
    t.simple = ct.getSimpleName().toString();
    t.fqn = outer != null ? outer.fqn + "." + t.simple : (pkg.isEmpty() ? t.simple : pkg + "." + t.simple);
    t.pkg = pkg;
    t.kind = ct.getKind().name().toLowerCase(Locale.ROOT).replace("annotation_type", "annotation");
    t.file = file;
    t.line = line(cu, ct);
    t.outer = outer;
    t.tree = ct;
    t.cu = cu;
    t.singleImports = single;
    t.starImports = star;
    if (ct.getExtendsClause() != null) t.extendsNames.addAll(typeNames(ct.getExtendsClause()).subList(0, 1));
    for (Tree i : ct.getImplementsClause()) {
      List<String> n = typeNames(i);
      if (!n.isEmpty()) t.implementsNames.add(n.get(0));
    }
    // Interfaces list their super-interfaces in the implements clause in javac's tree; treat as extends.
    if (ct.getKind() == Tree.Kind.INTERFACE) {
      t.extendsNames.addAll(t.implementsNames);
      t.implementsNames.clear();
    }
    types.put(t.fqn, t);

    TreeScanner<Void, Void> refScanner = new TreeScanner<>() {
      @Override public Void visitIdentifier(IdentifierTree node, Void v) {
        String n = node.getName().toString();
        if (!n.isEmpty() && Character.isUpperCase(n.charAt(0))) t.refs.add(n);
        return null;
      }
      @Override public Void visitNewClass(NewClassTree node, Void v) {
        List<String> n = typeNames(node.getIdentifier());
        if (!n.isEmpty()) t.news.add(n.get(0));
        return super.visitNewClass(node, v);
      }
      @Override public Void visitMemberSelect(MemberSelectTree node, Void v) {
        // Qualified type use such as SwerveRequest.FieldCentric
        String full = node.toString();
        if (full.matches("([A-Z]\\w*\\.)+[A-Z]\\w*")) t.refs.add(full);
        return super.visitMemberSelect(node, v);
      }
    };

    for (Tree m : ct.getMembers()) {
      if (m instanceof ClassTree nested) {
        collectType(nested, cu, pkg, single, star, file, t);
        continue;
      }
      if (m instanceof VariableTree vt) {
        FieldInfo f = new FieldInfo();
        f.name = vt.getName().toString();
        f.typeText = vt.getType() == null ? "" : vt.getType().toString();
        f.typeNames = typeNames(vt.getType());
        f.isStatic = vt.getModifiers().getFlags().contains(Modifier.STATIC);
        f.init = vt.getInitializer();
        if (f.init instanceof NewClassTree nc) {
          List<String> n = typeNames(nc.getIdentifier());
          if (!n.isEmpty()) f.newType = n.get(0);
        }
        f.line = line(cu, vt);
        t.fields.put(f.name, f);
      } else if (m instanceof MethodTree mt) {
        MethodInfo mi = new MethodInfo();
        mi.name = mt.getName().toString();
        mi.isConstructor = mi.name.equals("<init>");
        mi.returnType = mt.getReturnType() == null ? "" : mt.getReturnType().toString();
        Set<Modifier> flags = mt.getModifiers().getFlags();
        mi.isPublic = flags.contains(Modifier.PUBLIC)
            || (ct.getKind() == Tree.Kind.INTERFACE && !flags.contains(Modifier.PRIVATE));
        mi.tree = mt;
        mi.line = line(cu, mt);
        for (VariableTree p : mt.getParameters()) mi.paramTypeNames.add(typeNames(p.getType()));
        t.methods.add(mi);
      }
      m.accept(refScanner, null);
    }
  }

  /** Type names used in a type tree, outermost first (generics, arrays and annotations unwrapped). */
  static List<String> typeNames(Tree tree) {
    List<String> out = new ArrayList<>();
    collectTypeNames(tree, out);
    return out;
  }

  static void collectTypeNames(Tree tree, List<String> out) {
    if (tree == null) return;
    if (tree instanceof IdentifierTree id) {
      String n = id.getName().toString();
      if (!n.isEmpty() && Character.isUpperCase(n.charAt(0))) out.add(n);
    } else if (tree instanceof MemberSelectTree ms) {
      out.add(ms.toString());
    } else if (tree instanceof ParameterizedTypeTree pt) {
      collectTypeNames(pt.getType(), out);
      for (Tree a : pt.getTypeArguments()) collectTypeNames(a, out);
    } else if (tree instanceof ArrayTypeTree at) {
      collectTypeNames(at.getType(), out);
    } else if (tree instanceof WildcardTree wt) {
      collectTypeNames(wt.getBound(), out);
    } else if (tree instanceof AnnotatedTypeTree an) {
      collectTypeNames(an.getUnderlyingType(), out);
    } else if (tree instanceof UnionTypeTree ut) {
      for (Tree a : ut.getTypeAlternatives()) collectTypeNames(a, out);
    }
  }

  int line(CompilationUnitTree cu, Tree t) {
    long pos = positions.getStartPosition(cu, t);
    return pos < 0 ? 0 : (int) cu.getLineMap().getLineNumber(pos);
  }

  String text(CompilationUnitTree cu, Tree t) {
    long s = positions.getStartPosition(cu, t), e = positions.getEndPosition(cu, t);
    String src = sources.get(cu);
    if (s < 0 || e < 0 || src == null || e > src.length()) return t.toString();
    return src.substring((int) s, (int) e);
  }

  // ---------------------------------------------------------------------------------------------
  // Name resolution
  // ---------------------------------------------------------------------------------------------

  /** Resolves a simple or dotted type name used inside ctx to a parsed type, or null. */
  TypeInfo resolve(String name, TypeInfo ctx) {
    if (name == null || name.isEmpty()) return null;
    int dot = name.indexOf('.');
    if (dot > 0) {
      TypeInfo head = resolve(name.substring(0, dot), ctx);
      if (head != null) return types.get(head.fqn + name.substring(dot));
      return types.get(name); // fully qualified
    }
    for (TypeInfo o = ctx; o != null; o = o.outer) {
      TypeInfo n = types.get(o.fqn + "." + name);
      if (n != null) return n;
      if (o.simple.equals(name)) return o;
    }
    // Nested types inherited from parsed supertypes
    for (TypeInfo sup : parsedSupertypes(ctx, new HashSet<>())) {
      TypeInfo n = types.get(sup.fqn + "." + name);
      if (n != null) return n;
    }
    for (String imp : ctx.singleImports) {
      if (imp.endsWith("." + name)) return types.get(imp);
    }
    TypeInfo samePkg = types.get(ctx.pkg.isEmpty() ? name : ctx.pkg + "." + name);
    if (samePkg != null) return samePkg;
    for (String p : ctx.starImports) {
      TypeInfo n = types.get(p + "." + name);
      if (n != null) return n;
    }
    return null;
  }

  List<TypeInfo> parsedSupertypes(TypeInfo t, Set<String> seen) {
    List<TypeInfo> out = new ArrayList<>();
    if (t == null || !seen.add(t.fqn)) return out;
    for (String n : concat(t.extendsNames, t.implementsNames)) {
      TypeInfo s = resolveNoSuper(n, t);
      if (s != null) {
        out.add(s);
        out.addAll(parsedSupertypes(s, seen));
      }
    }
    return out;
  }

  /** resolve() without the inherited-nested-type step, to avoid recursion while walking supertypes. */
  TypeInfo resolveNoSuper(String name, TypeInfo ctx) {
    int dot = name.indexOf('.');
    if (dot > 0) {
      TypeInfo head = resolveNoSuper(name.substring(0, dot), ctx);
      return head != null ? types.get(head.fqn + name.substring(dot)) : types.get(name);
    }
    for (TypeInfo o = ctx; o != null; o = o.outer) {
      TypeInfo n = types.get(o.fqn + "." + name);
      if (n != null) return n;
    }
    for (String imp : ctx.singleImports) if (imp.endsWith("." + name)) return types.get(imp);
    TypeInfo samePkg = types.get(ctx.pkg.isEmpty() ? name : ctx.pkg + "." + name);
    if (samePkg != null) return samePkg;
    for (String p : ctx.starImports) {
      TypeInfo n = types.get(p + "." + name);
      if (n != null) return n;
    }
    return null;
  }

  /** Best-effort fully qualified name for a type that may not be parsed (e.g. a WPILib class). */
  String qualify(String name, TypeInfo ctx) {
    TypeInfo r = resolve(name, ctx);
    if (r != null) return r.fqn;
    String head = name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
    String tail = name.contains(".") ? name.substring(name.indexOf('.')) : "";
    for (String imp : ctx.singleImports) if (imp.endsWith("." + head)) return imp + tail;
    return name;
  }

  static <T> List<T> concat(List<T> a, List<T> b) {
    List<T> l = new ArrayList<>(a);
    l.addAll(b);
    return l;
  }

  /** Every supertype name reachable from t, qualified where possible (parsed and external). */
  Set<String> allSupertypes(TypeInfo t) {
    Set<String> out = new LinkedHashSet<>();
    Deque<TypeInfo> q = new ArrayDeque<>(List.of(t));
    Set<String> seen = new HashSet<>();
    while (!q.isEmpty()) {
      TypeInfo c = q.poll();
      if (!seen.add(c.fqn)) continue;
      for (String n : concat(c.extendsNames, c.implementsNames)) {
        out.add(qualify(n, c));
        TypeInfo r = resolveNoSuper(n, c);
        if (r != null) q.add(r);
      }
    }
    return out;
  }

  // ---------------------------------------------------------------------------------------------
  // FRC classification
  // ---------------------------------------------------------------------------------------------

  static final Set<String> ROBOT_BASES = Set.of("TimedRobot", "TimesliceRobot", "IterativeRobotBase",
      "RobotBase", "LoggedRobot", "CommandRobot");
  static final Set<String> SUBSYSTEM_BASES = Set.of("SubsystemBase", "Subsystem", "Mechanism");
  static final Pattern COMMAND_BASE = Pattern.compile(
      "(Command|CommandBase|\\w*CommandGroup|InstantCommand|RunCommand|StartEndCommand|"
          + "FunctionalCommand|WaitCommand|\\w*PIDCommand|ProfiledPIDCommand|TrapezoidProfileCommand|"
          + "ConditionalCommand|SelectCommand|DeferredCommand|ProxyCommand|RepeatCommand|"
          + "ScheduleCommand|WrapperCommand)");

  static String simpleOf(String q) {
    int i = q.lastIndexOf('.');
    return i < 0 ? q : q.substring(i + 1);
  }

  static boolean isWpilibCommandPkg(String q) {
    return q.startsWith("edu.wpi.first.wpilibj2.command") || q.startsWith("org.wpilib.command")
        || q.startsWith("edu.wpi.first.wpilibj.command");
  }

  boolean isSubsystem(TypeInfo t) {
    for (String s : allSupertypes(t)) {
      String simple = simpleOf(s);
      if (SUBSYSTEM_BASES.contains(simple) && (isWpilibCommandPkg(s) || !s.contains("."))) return true;
    }
    return false;
  }

  boolean isCommandClass(TypeInfo t) {
    for (String s : allSupertypes(t)) {
      if (COMMAND_BASE.matcher(simpleOf(s)).matches() && (isWpilibCommandPkg(s) || !s.contains(".")))
        return true;
    }
    return false;
  }

  boolean isRobotClass(TypeInfo t) {
    for (String s : allSupertypes(t)) if (ROBOT_BASES.contains(simpleOf(s))) return true;
    return false;
  }

  String categorize(TypeInfo t, TypeInfo root) {
    if (t == root) return "robot";
    String lowerPkg = t.pkg.toLowerCase(Locale.ROOT);
    boolean subsystem = isSubsystem(t);
    if (!subsystem && t.project && t.kind.equals("class")) {
      // A project class whose superclass is unparsed and named like a subsystem.
      for (String s : t.extendsNames) {
        TypeInfo r = resolve(s, t);
        if (r == null && simpleOf(s).endsWith("Subsystem") && !qualify(s, t).startsWith("java.")) {
          subsystem = true;
          t.heuristicSubsystem = true;
          warnings.add(t.simple + " treated as a subsystem because its superclass " + s
              + " is named like one; its source was not found to confirm.");
        }
      }
    }
    if (!t.project) return subsystem || isCommandClass(t) ? "library-base" : "library";
    if (subsystem) return "subsystem";
    if (t.simple.equals("RobotContainer")) return "container";
    if (isCommandClass(t)) return "command";
    if (t.simple.endsWith("Constants") || t.simple.equals("Constants") || lowerPkg.endsWith(".constants")
        || lowerPkg.contains(".constants.") || t.simple.equals("Ports")) return "constants";
    if (lowerPkg.contains(".commands") || lowerPkg.contains(".autos") || lowerPkg.endsWith(".auto")
        || t.simple.equals("Superstructure") || t.simple.endsWith("Commands")) return "command";
    if (lowerPkg.contains(".subsystems")) return "subsystem-part";
    return "other";
  }

  TypeInfo findRoot(String rootName) {
    if (rootName != null) {
      for (TypeInfo t : types.values())
        if (t.fqn.equals(rootName) || t.simple.equals(rootName)) return t;
      return null;
    }
    List<TypeInfo> robots = types.values().stream().filter(t -> t.outer == null)
        .filter(this::isRobotClass).collect(Collectors.toList());
    for (TypeInfo t : robots) if (t.simple.equals("Robot")) return t;
    if (!robots.isEmpty()) {
      if (robots.size() > 1)
        warnings.add("Several robot classes found; using " + robots.get(0).fqn + ". Pass --root to choose.");
      return robots.get(0);
    }
    for (TypeInfo t : types.values()) if (t.simple.equals("Robot") && t.outer == null) return t;
    return null;
  }

  Path sourceRootOf(TypeInfo root) {
    Path f = projectDir.resolve(root.file).getParent();
    if (!root.pkg.isEmpty()) {
      for (int i = 0; i < root.pkg.split("\\.").length && f != null; i++) f = f.getParent();
    }
    return f;
  }

  // ---------------------------------------------------------------------------------------------
  // Relationships and reachability
  // ---------------------------------------------------------------------------------------------

  Set<String> reach(TypeInfo root, List<Edge> edges) {
    Set<String> seen = new LinkedHashSet<>(List.of(root.fqn));
    Deque<TypeInfo> q = new ArrayDeque<>(List.of(root));
    while (!q.isEmpty()) {
      TypeInfo c = q.poll();
      Map<String, Edge> best = new LinkedHashMap<>();
      Function<TypeInfo, Void> enqueue = r -> {
        if (seen.add(r.fqn)) q.add(r);
        return null;
      };
      java.util.function.BiConsumer<String, String[]> link = (name, kl) -> {
        TypeInfo r = resolve(name, c);
        if (r == null || r == c) return;
        String kind = kl[0], label = kl.length > 1 ? kl[1] : null;
        Edge e = best.get(r.fqn);
        if (e == null || EDGE_ORDER.indexOf(kind) < EDGE_ORDER.indexOf(e.kind())) {
          Set<String> labels = new TreeSet<>(e == null ? Set.of() : e.labels());
          e = new Edge(c.fqn, r.fqn, kind, labels);
          best.put(r.fqn, e);
        }
        if (label != null && e.kind().equals(kind)) e.labels().add(label);
        enqueue.apply(r);
      };
      for (String s : c.extendsNames) link.accept(s, new String[] {"extends"});
      for (String s : c.implementsNames) link.accept(s, new String[] {"implements"});
      for (FieldInfo f : c.fields.values()) {
        if (f.isStatic && f.typeNames.isEmpty()) continue;
        String kind = f.newType != null ? "owns" : "has";
        for (int i = 0; i < f.typeNames.size(); i++) {
          String tn = f.typeNames.get(i);
          // Only the declared type itself "owns"; generic arguments are "has".
          link.accept(tn, new String[] {i == 0 ? kind : "has", f.name});
        }
        if (f.newType != null) link.accept(f.newType, new String[] {"owns", f.name});
      }
      for (MethodInfo m : c.methods)
        if (m.isConstructor)
          for (List<String> p : m.paramTypeNames) for (String s : p) link.accept(s, new String[] {"injected"});
      for (String s : c.news) link.accept(s, new String[] {"creates"});
      for (String s : c.refs) link.accept(s, new String[] {"uses"});
      if (c.outer != null) enqueue.apply(c.outer);
      edges.addAll(best.values());
    }
    return seen;
  }

  static final class Graph {
    String name;
    List<TypeInfo> nodes = new ArrayList<>();
    List<Edge> edges = new ArrayList<>();
    Map<String, List<String>> folded = new TreeMap<>();
  }

  TypeInfo top(TypeInfo t) {
    while (t.outer != null) t = t.outer;
    return t;
  }

  /**
   * Builds a view. Nested types are folded into their top-level class. The architecture view keeps
   * structural edges only, hides constants, and keeps library classes only when a project class
   * links to them directly. The full view keeps everything reachable.
   */
  Graph buildView(TypeInfo root, Set<String> reachable, List<Edge> raw, boolean full) {
    Graph g = new Graph();
    g.name = full ? "full" : "architecture";
    Set<String> structural = Set.of("extends", "implements", "owns", "has", "injected", "creates");
    Map<String, Edge> merged = new LinkedHashMap<>();
    for (Edge e : raw) {
      if (!full && !structural.contains(e.kind())) continue;
      TypeInfo a = top(types.get(e.src())), b = top(types.get(e.dst()));
      if (a == b) continue;
      String key = a.fqn + "|" + b.fqn;
      Edge m = merged.get(key);
      if (m == null || EDGE_ORDER.indexOf(e.kind()) < EDGE_ORDER.indexOf(m.kind())) {
        Set<String> labels = new TreeSet<>(e.labels());
        if (m != null && m.kind().equals(e.kind())) labels.addAll(m.labels());
        merged.put(key, new Edge(a.fqn, b.fqn, e.kind(), labels));
      } else if (m.kind().equals(e.kind())) m.labels().addAll(e.labels());
    }
    Set<String> keep = new LinkedHashSet<>();
    for (String id : reachable) {
      TypeInfo t = top(types.get(id));
      if (full) keep.add(t.fqn);
      else if (t.project && !t.category.equals("constants")) keep.add(t.fqn);
    }
    if (!full) {
      for (Edge e : merged.values()) {
        TypeInfo a = types.get(e.src()), b = types.get(e.dst());
        // Library classes appear only when project code is built on them or holds one, not
        // merely because it creates or calls into them.
        if (keep.contains(a.fqn) && a.project && !b.project && !e.kind().equals("creates")) keep.add(b.fqn);
      }
    }
    keep.add(root.fqn);
    for (String id : keep) g.nodes.add(types.get(id));
    for (Edge e : merged.values())
      if (keep.contains(e.src()) && keep.contains(e.dst())) g.edges.add(e);
    for (String id : reachable) {
      TypeInfo t = types.get(id);
      if (t.outer != null && keep.contains(top(t).fqn))
        g.folded.computeIfAbsent(top(t).fqn, k -> new ArrayList<>()).add(t.fqn.substring(top(t).fqn.length() + 1));
    }
    return g;
  }

  // ---------------------------------------------------------------------------------------------
  // Button bindings and default commands
  // ---------------------------------------------------------------------------------------------

  static final Map<String, String> BINDING_BEHAVIOR = new LinkedHashMap<>();
  static {
    BINDING_BEHAVIOR.put("onTrue", "press: starts the command (it runs until it ends or is interrupted)");
    BINDING_BEHAVIOR.put("onFalse", "release: starts the command");
    BINDING_BEHAVIOR.put("onChange", "press or release: starts the command");
    BINDING_BEHAVIOR.put("whileTrue", "held: runs while held, cancelled on release");
    BINDING_BEHAVIOR.put("whileFalse", "not held: runs while released, cancelled on press");
    BINDING_BEHAVIOR.put("toggleOnTrue", "press: toggles the command on/off");
    BINDING_BEHAVIOR.put("toggleOnFalse", "release: toggles the command on/off");
    // Legacy (pre-2023) names
    BINDING_BEHAVIOR.put("whenPressed", "press: starts the command (legacy API)");
    BINDING_BEHAVIOR.put("whenReleased", "release: starts the command (legacy API)");
    BINDING_BEHAVIOR.put("whileHeld", "held: runs repeatedly while held (legacy API)");
    BINDING_BEHAVIOR.put("whenHeld", "held: runs while held (legacy API)");
    BINDING_BEHAVIOR.put("toggleWhenPressed", "press: toggles the command (legacy API)");
    BINDING_BEHAVIOR.put("whenActive", "trigger becomes true: starts the command (legacy API)");
    BINDING_BEHAVIOR.put("whileActiveContinuous", "while true: runs repeatedly (legacy API)");
    BINDING_BEHAVIOR.put("whileActiveOnce", "while true: runs (legacy API)");
  }

  static final Map<String, String> FACE_HINT = Map.of(
      "faceDown", "south face button (A on Xbox)",
      "faceRight", "east face button (B on Xbox)",
      "faceLeft", "west face button (X on Xbox)",
      "faceUp", "north face button (Y on Xbox)");

  Map<String, Object> extractBindings(Set<String> reachable) {
    List<Object> bindings = new ArrayList<>();
    List<Object> defaults = new ArrayList<>();
    Map<String, Map<String, Object>> controllers = new LinkedHashMap<>();
    for (String id : reachable) {
      TypeInfo t = types.get(id);
      if (!t.project) continue;
      for (Tree member : t.tree.getMembers()) {
        if (member instanceof ClassTree) continue;
        member.accept(new TreeScanner<Void, Void>() {
          @Override public Void visitMethodInvocation(MethodInvocationTree mi, Void v) {
            String name = methodName(mi);
            if (BINDING_BEHAVIOR.containsKey(name) && mi.getArguments().size() == 1
                && mi.getMethodSelect() instanceof MemberSelectTree ms) {
              bindings.add(binding(t, ms.getExpression(), name, mi.getArguments().get(0), mi, controllers));
            } else if (name.equals("setDefaultCommand") && mi.getArguments().size() == 1) {
              defaults.add(defaultCommand(t, mi));
            }
            return super.visitMethodInvocation(mi, v);
          }
        }, null);
      }
    }
    bindings.sort(Comparator.comparing((Object o) -> String.valueOf(((Map<?, ?>) o).get("controllerPort")))
        .thenComparing(o -> String.valueOf(((Map<?, ?>) o).get("controller")))
        .thenComparing(o -> String.valueOf(((Map<?, ?>) o).get("file")))
        .thenComparingInt(o -> (Integer) ((Map<?, ?>) o).get("line")));
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("controllers", new ArrayList<>(controllers.values()));
    out.put("bindings", bindings);
    out.put("defaultCommands", defaults);
    return out;
  }

  static String methodName(MethodInvocationTree mi) {
    ExpressionTree s = mi.getMethodSelect();
    if (s instanceof MemberSelectTree ms) return ms.getIdentifier().toString();
    if (s instanceof IdentifierTree id) return id.getName().toString();
    return "";
  }

  Map<String, Object> binding(TypeInfo ctx, ExpressionTree trigger, String how, ExpressionTree cmd,
      Tree site, Map<String, Map<String, Object>> controllers) {
    Map<String, Object> b = new LinkedHashMap<>();
    String[] ctrl = new String[3]; // field, type, port
    String pretty = prettyTrigger(ctx, trigger, ctrl);
    if (ctrl[0] != null) {
      Map<String, Object> c = controllers.computeIfAbsent(ctx.fqn + "#" + ctrl[0], k -> {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", ctrl[0]);
        m.put("type", ctrl[1]);
        m.put("port", ctrl[2]);
        m.put("declaredIn", ctx.fqn);
        return m;
      });
      b.put("controller", c.get("name"));
      b.put("controllerType", c.get("type"));
      b.put("controllerPort", c.get("port"));
    } else {
      b.put("controller", "(custom trigger)");
      b.put("controllerType", "");
      b.put("controllerPort", "");
    }
    b.put("trigger", pretty);
    String hint = null;
    for (Map.Entry<String, String> e : FACE_HINT.entrySet())
      if (pretty.matches(".*\\b" + e.getKey() + "\\b.*")) hint = (hint == null ? "" : hint + "; ") + e.getKey() + " = " + e.getValue();
    if (hint != null) b.put("triggerHint", hint);
    b.put("binding", how);
    b.put("behavior", BINDING_BEHAVIOR.get(how));
    b.put("command", squash(text(ctx.cu, cmd)));
    Map<String, Object> mech = mechanisms(ctx, cmd);
    b.put("mechanisms", mech.get("mechanisms"));
    b.put("commandMethods", mech.get("methods"));
    b.put("declaredIn", ctx.fqn);
    b.put("file", ctx.file);
    b.put("line", line(ctx.cu, site));
    return b;
  }

  Map<String, Object> defaultCommand(TypeInfo ctx, MethodInvocationTree mi) {
    Map<String, Object> d = new LinkedHashMap<>();
    String target = ctx.simple;
    String targetFqn = ctx.fqn;
    if (mi.getMethodSelect() instanceof MemberSelectTree ms) {
      ExpressionTree recv = ms.getExpression();
      String recvText = text(ctx.cu, recv);
      target = recvText;
      TypeInfo tt = fieldType(ctx, rootIdentifier(recv));
      if (tt != null && recv instanceof IdentifierTree) targetFqn = tt.fqn;
      else if (recvText.equals("this")) targetFqn = ctx.fqn;
      else targetFqn = null;
    }
    d.put("mechanismField", target);
    d.put("mechanism", targetFqn);
    ExpressionTree cmd = mi.getArguments().get(0);
    d.put("command", squash(text(ctx.cu, cmd)));
    d.put("mechanisms", mechanisms(ctx, cmd).get("mechanisms"));
    d.put("declaredIn", ctx.fqn);
    d.put("file", ctx.file);
    d.put("line", line(ctx.cu, mi));
    return d;
  }

  static String squash(String s) {
    s = s.replaceAll("\\s+", " ").replaceAll("\\( ", "(").replaceAll(" \\)", ")").replaceAll("\\. ", ".").trim();
    return s.length() > 220 ? s.substring(0, 217) + "..." : s;
  }

  static String rootIdentifier(ExpressionTree e) {
    while (true) {
      if (e instanceof MethodInvocationTree mi) e = mi.getMethodSelect();
      else if (e instanceof MemberSelectTree ms) {
        if (ms.getExpression() instanceof IdentifierTree id && id.getName().contentEquals("this"))
          return ms.getIdentifier().toString();
        e = ms.getExpression();
      } else if (e instanceof ParenthesizedTree p) e = p.getExpression();
      else if (e instanceof IdentifierTree id) return id.getName().toString();
      else return null;
    }
  }

  FieldInfo findField(TypeInfo ctx, String name) {
    if (name == null) return null;
    for (TypeInfo o = ctx; o != null; o = o.outer) {
      FieldInfo f = o.fields.get(name);
      if (f != null) return f;
      for (TypeInfo s : parsedSupertypes(o, new HashSet<>())) {
        FieldInfo sf = s.fields.get(name);
        if (sf != null) return sf;
      }
    }
    return null;
  }

  TypeInfo fieldType(TypeInfo ctx, String name) {
    FieldInfo f = findField(ctx, name);
    if (f == null || f.typeNames.isEmpty()) return null;
    return resolve(f.typeNames.get(0), ctx);
  }

  /** Readable trigger text, e.g. "start + not leftBumper". Fills ctrl[] with field, type, port. */
  String prettyTrigger(TypeInfo ctx, ExpressionTree e, String[] ctrl) {
    if (e instanceof ParenthesizedTree p) return prettyTrigger(ctx, p.getExpression(), ctrl);
    if (e instanceof MethodInvocationTree mi && mi.getMethodSelect() instanceof MemberSelectTree ms) {
      String m = ms.getIdentifier().toString();
      ExpressionTree recv = ms.getExpression();
      List<? extends ExpressionTree> args = mi.getArguments();
      switch (m) {
        case "and": return prettyTrigger(ctx, recv, ctrl) + " + " + prettyArg(ctx, args, ctrl);
        case "or": return prettyTrigger(ctx, recv, ctrl) + " or " + prettyArg(ctx, args, ctrl);
        case "negate": return "not " + prettyTrigger(ctx, recv, ctrl);
        case "debounce": return prettyTrigger(ctx, recv, ctrl) + " (debounced)";
        case "castTo": return prettyTrigger(ctx, recv, ctrl);
        default:
      }
      if (recv instanceof IdentifierTree || (recv instanceof MemberSelectTree rs
          && rs.getExpression() instanceof IdentifierTree ti && ti.getName().contentEquals("this"))) {
        noteController(ctx, rootIdentifier(recv), ctrl);
        String a = args.isEmpty() ? "" : "(" + args.stream().map(x -> squash(text(ctx.cu, x))).collect(Collectors.joining(", ")) + ")";
        return m + a;
      }
    }
    if (e instanceof NewClassTree nc) {
      String type = simpleOf(nc.getIdentifier().toString());
      List<? extends ExpressionTree> args = nc.getArguments();
      if ((type.equals("JoystickButton") || type.equals("POVButton")) && args.size() >= 2) {
        noteController(ctx, rootIdentifier(args.get(0)), ctrl);
        return (type.equals("POVButton") ? "POV " : "button ") + squash(text(ctx.cu, args.get(1)));
      }
      if (type.equals("Trigger") && args.size() == 1) {
        return "custom: " + squash(text(ctx.cu, args.get(0)));
      }
    }
    if (e instanceof IdentifierTree id) {
      // A Trigger stored in a field/variable; keep its name.
      return id.getName().toString();
    }
    return squash(text(ctx.cu, e));
  }

  String prettyArg(TypeInfo ctx, List<? extends ExpressionTree> args, String[] ctrl) {
    if (args.size() != 1) return "?";
    String[] other = new String[3];
    String s = prettyTrigger(ctx, args.get(0), other);
    if (other[0] != null && ctrl[0] != null && !other[0].equals(ctrl[0])) s = other[0] + "." + s;
    if (ctrl[0] == null) System.arraycopy(other, 0, ctrl, 0, 3);
    return s;
  }

  void noteController(TypeInfo ctx, String fieldName, String[] ctrl) {
    if (fieldName == null || ctrl[0] != null) return;
    FieldInfo f = findField(ctx, fieldName);
    if (f == null) return;
    ctrl[0] = fieldName;
    ctrl[1] = f.typeNames.isEmpty() ? f.typeText : simpleOf(f.typeNames.get(0));
    ctrl[2] = "";
    if (f.init instanceof NewClassTree nc && !nc.getArguments().isEmpty()) {
      ctrl[2] = constantValue(ctx, nc.getArguments().get(0), 0);
    }
  }

  /** Evaluates a literal or a reference to a constant initialised with a literal. */
  String constantValue(TypeInfo ctx, ExpressionTree e, int depth) {
    if (depth > 4) return squash(text(ctx.cu, e));
    if (e instanceof LiteralTree lt) return String.valueOf(lt.getValue());
    if (e instanceof ParenthesizedTree p) return constantValue(ctx, p.getExpression(), depth + 1);
    if (e instanceof IdentifierTree id) {
      FieldInfo f = findField(ctx, id.getName().toString());
      if (f != null && f.init != null) return constantValue(ctx, f.init, depth + 1);
    }
    if (e instanceof MemberSelectTree ms) {
      TypeInfo owner = resolve(ms.getExpression().toString(), ctx);
      if (owner != null) {
        FieldInfo f = owner.fields.get(ms.getIdentifier().toString());
        if (f != null && f.init != null) return constantValue(owner, f.init, depth + 1);
      }
    }
    return squash(text(ctx.cu, e));
  }

  /**
   * Which mechanisms (subsystems) a command expression refers to: subsystem fields it names, and —
   * one call at a time, up to a few levels deep — the subsystem fields used by the project methods
   * it calls. This is a static estimate, not the command's declared requirements.
   */
  Map<String, Object> mechanisms(TypeInfo ctx, ExpressionTree expr) {
    Set<String> mechs = new TreeSet<>();
    Set<String> methods = new LinkedHashSet<>();
    scanMechs(ctx, expr, 0, new HashSet<>(), mechs, methods);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("mechanisms", new ArrayList<>(mechs));
    m.put("methods", new ArrayList<>(methods));
    return m;
  }

  void scanMechs(TypeInfo ctx, Tree tree, int depth, Set<String> visited, Set<String> mechs,
      Set<String> methods) {
    if (tree == null || depth > 5) return;
    tree.accept(new TreeScanner<Void, Void>() {
      @Override public Void visitIdentifier(IdentifierTree id, Void v) {
        String n = id.getName().toString();
        if (n.equals("this")) {
          if (ctx.category.equals("subsystem")) mechs.add(ctx.fqn);
          return null;
        }
        TypeInfo ft = fieldType(ctx, n);
        if (ft != null && isMechanism(ft)) mechs.add(ft.fqn);
        return null;
      }
      @Override public Void visitMethodInvocation(MethodInvocationTree mi, Void v) {
        ExpressionTree sel = mi.getMethodSelect();
        String name = methodName(mi);
        TypeInfo target = null;
        if (sel instanceof IdentifierTree) target = ctx; // unqualified call within this class
        else if (sel instanceof MemberSelectTree ms) {
          ExpressionTree recv = ms.getExpression();
          if (recv instanceof IdentifierTree rid) {
            String rn = rid.getName().toString();
            if (rn.equals("this")) target = ctx;
            else {
              target = fieldType(ctx, rn);
              if (target == null) {
                TypeInfo stat = resolve(rn, ctx); // static call such as Autos.shoot(...)
                if (stat != null && stat.project) target = stat;
              }
            }
          }
        }
        if (target != null) {
          if (isMechanism(target)) {
            mechs.add(target.fqn);
            if (target != ctx || sel instanceof MemberSelectTree) methods.add(target.simple + "." + name);
          } else if (target.project) {
            methods.add(target.simple + "." + name);
            for (MethodInfo m : methodsNamed(target, name)) {
              String key = target.fqn + "#" + name + "#" + m.line;
              if (m.tree.getBody() != null && visited.add(key))
                scanMechs(target, m.tree.getBody(), depth + 1, visited, mechs, methods);
            }
          }
        }
        return super.visitMethodInvocation(mi, v);
      }
      @Override public Void visitNewClass(NewClassTree nc, Void v) {
        TypeInfo t = resolve(typeNames(nc.getIdentifier()).stream().findFirst().orElse(""), ctx);
        if (t != null && t.project) methods.add("new " + t.simple);
        return super.visitNewClass(nc, v);
      }
    }, null);
  }

  boolean isMechanism(TypeInfo t) {
    return t.category.equals("subsystem") || (t.category.equals("library-base") && isSubsystem(t));
  }

  List<MethodInfo> methodsNamed(TypeInfo t, String name) {
    List<MethodInfo> out = new ArrayList<>();
    for (MethodInfo m : t.methods) if (m.name.equals(name)) out.add(m);
    if (out.isEmpty())
      for (TypeInfo s : parsedSupertypes(t, new HashSet<>()))
        for (MethodInfo m : s.methods) if (m.name.equals(name)) out.add(m);
    return out;
  }

  // ---------------------------------------------------------------------------------------------
  // Project metadata and JSON
  // ---------------------------------------------------------------------------------------------

  Map<String, Object> projectMeta(TypeInfo root) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("name", projectDir.getFileName() == null ? "" : projectDir.getFileName().toString());
    String wpilib = "unknown";
    for (String f : List.of("build.gradle", "build.gradle.kts")) {
      Path p = projectDir.resolve(f);
      if (Files.isRegularFile(p)) {
        try {
          Matcher mt = Pattern.compile("GradleRIO\"?\\)?\\s*version\\s*\\(?\\s*\"([^\"]+)\"")
              .matcher(Files.readString(p));
          if (mt.find()) wpilib = mt.group(1);
        } catch (IOException ignored) {
        }
      }
    }
    m.put("wpilibVersion", wpilib);
    String team = "";
    Path prefs = projectDir.resolve(".wpilib").resolve("wpilib_preferences.json");
    if (Files.isRegularFile(prefs)) {
      try {
        Matcher mt = Pattern.compile("\"teamNumber\"\\s*:\\s*(\\d+)").matcher(Files.readString(prefs));
        if (mt.find()) team = mt.group(1);
      } catch (IOException ignored) {
      }
    }
    m.put("teamNumber", team);
    String fw;
    Set<String> kinds = frameworkImports.keySet().stream()
        .map(k -> k.substring(0, k.indexOf(' '))).collect(Collectors.toCollection(TreeSet::new));
    if (kinds.isEmpty()) fw = "none detected";
    else fw = String.join(" + ", kinds.stream().map(k -> "Commands " + k).toList());
    m.put("commandFramework", fw);
    m.put("commandFrameworkImports", frameworkImports);
    long edu = types.values().stream().filter(t -> t.project)
        .flatMap(t -> t.singleImports.stream()).filter(i -> i.startsWith("edu.wpi.first")).count();
    long org = types.values().stream().filter(t -> t.project)
        .flatMap(t -> t.singleImports.stream()).filter(i -> i.startsWith("org.wpilib")).count();
    m.put("wpilibNamespace", org > 0 && edu == 0 ? "org.wpilib (2027+)"
        : edu > 0 && org == 0 ? "edu.wpi.first (2026 and earlier)"
        : org > 0 ? "mixed: org.wpilib and edu.wpi.first" : "unknown");
    m.put("rootFile", root.file);
    m.put("parsedFiles", types.values().stream().map(t -> t.file).distinct().count());
    m.put("javaVersionUsed", Runtime.version().toString());
    return m;
  }

  List<Object> classesJson(Set<String> reachable) {
    List<Object> out = new ArrayList<>();
    for (String id : reachable) {
      TypeInfo t = types.get(id);
      Map<String, Object> c = new LinkedHashMap<>();
      c.put("id", t.fqn);
      c.put("name", t.simple);
      c.put("kind", t.kind);
      c.put("category", t.category);
      c.put("origin", t.origin);
      c.put("file", t.file);
      c.put("line", t.line);
      if (t.outer != null) c.put("outer", t.outer.fqn);
      c.put("extends", t.extendsNames.stream().map(n -> qualify(n, t)).toList());
      c.put("implements", t.implementsNames.stream().map(n -> qualify(n, t)).toList());
      if (t.heuristicSubsystem) c.put("subsystemByName", true);
      List<Object> fields = new ArrayList<>();
      for (FieldInfo f : t.fields.values()) {
        if (f.isStatic && t.category.equals("constants")) continue;
        Map<String, Object> fm = new LinkedHashMap<>();
        fm.put("name", f.name);
        fm.put("type", squash(f.typeText));
        TypeInfo r = f.typeNames.isEmpty() ? null : resolve(f.typeNames.get(0), t);
        if (r != null) fm.put("typeId", r.fqn);
        if (f.isStatic) fm.put("static", true);
        fm.put("line", f.line);
        fields.add(fm);
      }
      c.put("fields", fields);
      List<Object> methods = new ArrayList<>();
      for (MethodInfo m : t.methods) {
        if (!m.isPublic || m.isConstructor) continue;
        Map<String, Object> mm = new LinkedHashMap<>();
        mm.put("name", m.name);
        mm.put("returns", m.returnType);
        if (simpleOf(m.returnType.replaceAll("<.*", "")).endsWith("Command")) mm.put("commandFactory", true);
        mm.put("line", m.line);
        methods.add(mm);
      }
      c.put("publicMethods", methods);
      if (t.category.equals("constants")) {
        c.put("constantCount", t.fields.values().stream().filter(f -> f.isStatic).count());
      }
      out.add(c);
    }
    return out;
  }

  Map<String, Object> viewJson(Graph g) {
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("nodes", g.nodes.stream().map(t -> t.fqn).toList());
    List<Object> es = new ArrayList<>();
    for (Edge e : g.edges) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("from", e.src());
      m.put("to", e.dst());
      m.put("kind", e.kind());
      if (!e.labels().isEmpty()) m.put("via", new ArrayList<>(e.labels()));
      es.add(m);
    }
    v.put("edges", es);
    v.put("foldedNestedTypes", g.folded);
    return v;
  }

  // ---------------------------------------------------------------------------------------------
  // Layout and SVG
  // ---------------------------------------------------------------------------------------------

  static final List<String[]> TIERS = List.of(
      new String[] {"robot", "Entry point"},
      new String[] {"container", "Wiring (bindings & defaults)"},
      new String[] {"command", "Commands & coordination"},
      new String[] {"subsystem", "Mechanisms (subsystems)"},
      new String[] {"subsystem-part", "Mechanism parts"},
      new String[] {"other", "Helpers & other project code"},
      new String[] {"constants", "Constants"},
      new String[] {"library-base", "Library base classes"},
      new String[] {"library", "Library code"});

  static final class Layout {
    static final int NODE_H = 46, ROW_GAP = 74, COL_GAP = 22, BAND_PAD = 14, LEFT = 190,
        MAX_ROW_W = 1500, CHAR_W = 7;

    final Graph g;
    final FrcVisualize v;
    final String prefix;
    final Map<String, double[]> pos = new HashMap<>(); // x, y, w, h
    final Map<String, Integer> index = new HashMap<>();
    final List<double[]> bands = new ArrayList<>(); // y, h
    final List<String> bandLabels = new ArrayList<>();
    final List<List<String>> drawnRows = new ArrayList<>(); // node ids per drawn row, top to bottom
    final List<Double> rowTops = new ArrayList<>();
    double width, height;

    Layout(Graph g, FrcVisualize v, String prefix) {
      this.g = g;
      this.v = v;
      this.prefix = prefix;
      for (int i = 0; i < g.nodes.size(); i++) index.put(g.nodes.get(i).fqn, i);
      layout();
    }

    String stereotype(TypeInfo t) {
      List<String> sup = new ArrayList<>();
      for (String n : concat(t.extendsNames, t.implementsNames)) sup.add(simpleOf(n));
      String k = t.kind.equals("class") ? "" : t.kind;
      if (sup.isEmpty()) return k.isEmpty() ? "" : "«" + k + "»";
      return (k.isEmpty() ? "" : k + " ") + "◁ " + String.join(", ", sup);
    }

    double nodeWidth(TypeInfo t) {
      int n = Math.max(t.simple.length() * 8 / 7 + 1, (int) (stereotype(t).length() * 0.85));
      return Math.max(96, n * CHAR_W + 26);
    }

    void layout() {
      Map<String, List<String>> adj = new HashMap<>();
      for (Edge e : g.edges) {
        adj.computeIfAbsent(e.src(), k -> new ArrayList<>()).add(e.dst());
        adj.computeIfAbsent(e.dst(), k -> new ArrayList<>()).add(e.src());
      }
      double y = 16;
      double maxW = 0;
      List<List<List<TypeInfo>>> tierRows = new ArrayList<>();
      for (String[] tier : TIERS) {
        List<TypeInfo> ns = g.nodes.stream().filter(t -> t.category.equals(tier[0]))
            .sorted(Comparator.comparing(t -> t.simple)).collect(Collectors.toList());
        if (ns.isEmpty()) continue;
        // Order by the mean x of already-placed neighbours (barycentre), keeping name order for ties.
        Map<String, Double> bary = new HashMap<>();
        for (TypeInfo t : ns) {
          double sum = 0;
          int cnt = 0;
          for (String n : adj.getOrDefault(t.fqn, List.of())) {
            double[] p = pos.get(n);
            if (p != null) {
              sum += p[0] + p[2] / 2;
              cnt++;
            }
          }
          bary.put(t.fqn, cnt == 0 ? Double.MAX_VALUE : sum / cnt);
        }
        ns.sort(Comparator.comparing((TypeInfo t) -> bary.get(t.fqn)).thenComparing(t -> t.simple));
        // Wrap into rows.
        List<List<TypeInfo>> rows = new ArrayList<>();
        List<TypeInfo> row = new ArrayList<>();
        double rw = 0;
        for (TypeInfo t : ns) {
          double w = nodeWidth(t);
          if (!row.isEmpty() && rw + COL_GAP + w > MAX_ROW_W) {
            rows.add(row);
            row = new ArrayList<>();
            rw = 0;
          }
          rw += (row.isEmpty() ? 0 : COL_GAP) + w;
          row.add(t);
        }
        rows.add(row);
        double bandTop = y;
        for (List<TypeInfo> r : rows) {
          double total = r.stream().mapToDouble(this::nodeWidth).sum() + COL_GAP * (r.size() - 1);
          maxW = Math.max(maxW, total);
          double x = 0;
          drawnRows.add(r.stream().map(t -> t.fqn).collect(Collectors.toList()));
          rowTops.add(y + BAND_PAD);
          for (TypeInfo t : r) {
            double w = nodeWidth(t);
            pos.put(t.fqn, new double[] {x, y + BAND_PAD, w, NODE_H, total});
            x += w + COL_GAP;
          }
          y += NODE_H + BAND_PAD * 2;
          if (rows.indexOf(r) < rows.size() - 1) y += 26;
        }
        bands.add(new double[] {bandTop, y - bandTop});
        bandLabels.add(tier[1]);
        tierRows.add(rows);
        y += ROW_GAP - BAND_PAD * 2;
      }
      // Centre each row within the widest row.
      for (double[] p : pos.values()) p[0] += LEFT + (maxW - p[4]) / 2;
      width = LEFT + maxW + 30;
      height = y - ROW_GAP + BAND_PAD * 2 + 10;
    }

    String svg() {
      StringBuilder s = new StringBuilder();
      s.append("<svg xmlns=\"http://www.w3.org/2000/svg\" class=\"frcviz\" data-view=\"").append(prefix)
          .append("\" viewBox=\"0 0 ").append(fmt(width)).append(' ').append(fmt(height))
          .append("\" width=\"").append(fmt(width)).append("\" height=\"").append(fmt(height))
          .append("\" font-family=\"system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif\">\n");
      s.append("<title>Robot code structure (").append(esc(g.name)).append(" view)</title>\n");
      s.append(Html.SVG_STYLE);
      s.append("<defs>\n")
          .append(marker(prefix + "-vee", "M0,0 L10,5 L0,10", false))
          .append(marker(prefix + "-tri", "M0,0 L10,5 L0,10 Z", true))
          .append("<marker id=\"").append(prefix).append("-dia\" viewBox=\"0 0 14 10\" refX=\"1\" refY=\"5\" markerWidth=\"12\" markerHeight=\"9\" orient=\"auto-start-reverse\"><path d=\"M0,5 L7,0 L14,5 L7,10 Z\" class=\"mk-fill\"/></marker>\n")
          .append("<marker id=\"").append(prefix).append("-odia\" viewBox=\"0 0 14 10\" refX=\"1\" refY=\"5\" markerWidth=\"12\" markerHeight=\"9\" orient=\"auto-start-reverse\"><path d=\"M0,5 L7,0 L14,5 L7,10 Z\" class=\"mk-hollow\"/></marker>\n")
          .append("</defs>\n");
      for (int i = 0; i < bands.size(); i++) {
        double[] b = bands.get(i);
        s.append("<g class=\"band\"><rect x=\"6\" y=\"").append(fmt(b[0])).append("\" width=\"")
            .append(fmt(width - 12)).append("\" height=\"").append(fmt(b[1]))
            .append("\" rx=\"10\"/><text x=\"18\" y=\"").append(fmt(b[0] + b[1] / 2 + 4)).append("\">")
            .append(esc(bandLabels.get(i))).append("</text></g>\n");
      }
      // Edge ports: spread multiple edges along each node's side, ordered by the other end's x.
      Map<String, List<Object[]>> ports = new HashMap<>();
      List<Object[]> routes = new ArrayList<>();
      for (Edge e : g.edges) {
        double[] a = pos.get(e.src()), b = pos.get(e.dst());
        if (a == null || b == null) continue;
        boolean down = b[1] > a[1] + 1, up = b[1] < a[1] - 1;
        String sa = down ? "b" : up ? "t" : "t", sb = down ? "t" : up ? "b" : "t";
        Object[] r = {e, sa, sb, 0.0, 0.0};
        routes.add(r);
        ports.computeIfAbsent(e.src() + "|" + sa, k -> new ArrayList<>()).add(new Object[] {r, 3, b[0] + b[2] / 2});
        ports.computeIfAbsent(e.dst() + "|" + sb, k -> new ArrayList<>()).add(new Object[] {r, 4, a[0] + a[2] / 2});
      }
      for (Map.Entry<String, List<Object[]>> pe : ports.entrySet()) {
        String id = pe.getKey().substring(0, pe.getKey().lastIndexOf('|'));
        double[] p = pos.get(id);
        List<Object[]> l = pe.getValue();
        l.sort(Comparator.comparingDouble(o -> (Double) o[2]));
        for (int i = 0; i < l.size(); i++) {
          Object[] r = (Object[]) l.get(i)[0];
          r[(Integer) l.get(i)[1]] = p[0] + p[2] * (i + 1) / (l.size() + 1);
        }
      }
      s.append("<g class=\"edges\">\n");
      for (Object[] r : routes) {
        Edge e = (Edge) r[0];
        double[] a = pos.get(e.src()), b = pos.get(e.dst());
        double x1 = (Double) r[3], x2 = (Double) r[4];
        double y1 = r[1].equals("b") ? a[1] + a[3] : a[1];
        double y2 = r[2].equals("t") ? b[1] : b[1] + b[3];
        String d;
        if (Math.abs(a[1] - b[1]) > 1) {
          d = routeThroughGaps(x1, y1, x2, y2);
        } else if (Math.abs(a[1] - b[1]) < 1) { // same row: arc above
          double lift = 30 + Math.abs(x2 - x1) * 0.12;
          d = "M" + fmt(x1) + "," + fmt(y1) + " C" + fmt(x1) + "," + fmt(y1 - lift) + " " + fmt(x2) + ","
              + fmt(y2 - lift) + " " + fmt(x2) + "," + fmt(y2);
        } else {
          double dy = (y2 - y1) * 0.5;
          d = "M" + fmt(x1) + "," + fmt(y1) + " C" + fmt(x1) + "," + fmt(y1 + dy) + " " + fmt(x2) + ","
              + fmt(y2 - dy) + " " + fmt(x2) + "," + fmt(y2);
        }
        String start = switch (e.kind()) {
          case "owns" -> " marker-start=\"url(#" + prefix + "-dia)\"";
          case "has" -> " marker-start=\"url(#" + prefix + "-odia)\"";
          default -> "";
        };
        String end = e.kind().equals("extends") || e.kind().equals("implements")
            ? " marker-end=\"url(#" + prefix + "-tri)\"" : " marker-end=\"url(#" + prefix + "-vee)\"";
        String via = e.labels().isEmpty() ? "" : " via " + String.join(", ", e.labels());
        s.append("<g class=\"edge k-").append(e.kind()).append("\" data-from=\"").append(esc(e.src()))
            .append("\" data-to=\"").append(esc(e.dst())).append("\"><title>")
            .append(esc(simpleOf(e.src()) + " " + EDGE_WORDS.get(e.kind()) + " " + simpleOf(e.dst()) + via))
            .append("</title><path class=\"hit\" d=\"").append(d).append("\"/><path class=\"line\" d=\"")
            .append(d).append("\"").append(start).append(end).append("/></g>\n");
      }
      s.append("</g>\n<g class=\"nodes\">\n");
      for (TypeInfo t : g.nodes) {
        double[] p = pos.get(t.fqn);
        String st = stereotype(t);
        int folded = g.folded.getOrDefault(t.fqn, List.of()).size();
        s.append("<g class=\"node cat-").append(t.category).append("\" data-id=\"").append(esc(t.fqn))
            .append("\" tabindex=\"0\"><title>").append(esc(t.fqn + "\n" + t.file + ":" + t.line
                + (folded > 0 ? "\n+" + folded + " nested type(s)" : "")))
            .append("</title><rect x=\"").append(fmt(p[0])).append("\" y=\"").append(fmt(p[1]))
            .append("\" width=\"").append(fmt(p[2])).append("\" height=\"").append(fmt(p[3]))
            .append("\" rx=\"8\"/>");
        double cx = p[0] + p[2] / 2;
        if (st.isEmpty()) {
          s.append("<text class=\"nm\" x=\"").append(fmt(cx)).append("\" y=\"").append(fmt(p[1] + p[3] / 2 + 5))
              .append("\">").append(esc(t.simple)).append("</text>");
        } else {
          s.append("<text class=\"nm\" x=\"").append(fmt(cx)).append("\" y=\"").append(fmt(p[1] + 20))
              .append("\">").append(esc(t.simple)).append("</text><text class=\"st\" x=\"").append(fmt(cx))
              .append("\" y=\"").append(fmt(p[1] + 36)).append("\">").append(esc(st)).append("</text>");
        }
        if (folded > 0) {
          s.append("<text class=\"badge\" x=\"").append(fmt(p[0] + p[2] - 6)).append("\" y=\"")
              .append(fmt(p[1] + 12)).append("\">+").append(folded).append("</text>");
        }
        s.append("</g>\n");
      }
      s.append("</g>\n</svg>\n");
      return s.toString();
    }

    /**
     * An edge that crosses intermediate rows passes through the gap nearest its straight line in
     * each row, so it never runs behind an unrelated box.
     */
    String routeThroughGaps(double x1, double y1, double x2, double y2) {
      List<double[]> pts = new ArrayList<>();
      pts.add(new double[] {x1, y1});
      boolean down = y2 > y1;
      List<Integer> order = new ArrayList<>();
      for (int i = 0; i < drawnRows.size(); i++) order.add(i);
      if (!down) Collections.reverse(order);
      for (int i : order) {
        double top = rowTops.get(i), mid = top + NODE_H / 2.0;
        if (!(down ? (top > y1 + 1 && top + NODE_H < y2 - 1) : (top + NODE_H < y1 - 1 && top > y2 + 1)))
          continue;
        List<double[]> boxes = new ArrayList<>();
        for (String id : drawnRows.get(i)) boxes.add(pos.get(id));
        boxes.sort(Comparator.comparingDouble(bx -> bx[0]));
        double t = (mid - y1) / (y2 - y1), ideal = x1 + (x2 - x1) * t;
        boolean blocked = false;
        // Curves bow away from the straight line, so keep a generous margin around each box.
        for (double[] bx : boxes) if (ideal > bx[0] - 40 && ideal < bx[0] + bx[2] + 40) blocked = true;
        if (!blocked) continue;
        List<Double> gaps = new ArrayList<>();
        gaps.add(boxes.get(0)[0] - 16);
        for (int k = 0; k + 1 < boxes.size(); k++) {
          double l = boxes.get(k)[0] + boxes.get(k)[2], r = boxes.get(k + 1)[0];
          if (r - l >= 8) gaps.add((l + r) / 2);
        }
        double[] last = boxes.get(boxes.size() - 1);
        gaps.add(last[0] + last[2] + 16);
        double best = gaps.get(0);
        for (double gx : gaps) if (Math.abs(gx - ideal) < Math.abs(best - ideal)) best = gx;
        pts.add(new double[] {best, mid});
      }
      pts.add(new double[] {x2, y2});
      StringBuilder d = new StringBuilder("M" + fmt(x1) + "," + fmt(y1));
      for (int i = 1; i < pts.size(); i++) {
        double[] p0 = pts.get(i - 1), p1 = pts.get(i);
        double dy = (p1[1] - p0[1]) * 0.5;
        d.append(" C").append(fmt(p0[0])).append(',').append(fmt(p0[1] + dy)).append(' ')
            .append(fmt(p1[0])).append(',').append(fmt(p1[1] - dy)).append(' ')
            .append(fmt(p1[0])).append(',').append(fmt(p1[1]));
      }
      return d.toString();
    }

    String marker(String id, String path, boolean hollow) {
      return "<marker id=\"" + id + "\" viewBox=\"0 0 10 10\" refX=\"9\" refY=\"5\" markerWidth=\"9\""
          + " markerHeight=\"9\" orient=\"auto-start-reverse\"><path d=\"" + path + "\" class=\""
          + (hollow ? "mk-hollow" : "mk-line") + "\"/></marker>\n";
    }
  }

  static String fmt(double d) {
    return d == Math.rint(d) ? String.valueOf((long) d) : String.format(Locale.ROOT, "%.1f", d);
  }

  static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  // ---------------------------------------------------------------------------------------------
  // Minimal JSON writer (sorted, stable output so regenerated files diff cleanly)
  // ---------------------------------------------------------------------------------------------

  static final class Json {
    static String write(Object o) {
      StringBuilder sb = new StringBuilder();
      write(o, sb, 0);
      return sb.toString();
    }

    static void write(Object o, StringBuilder sb, int ind) {
      if (o == null) sb.append("null");
      else if (o instanceof String s) str(s, sb);
      else if (o instanceof Number || o instanceof Boolean) sb.append(o);
      else if (o instanceof Map<?, ?> m) {
        if (m.isEmpty()) {
          sb.append("{}");
          return;
        }
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<?, ?> e : m.entrySet()) {
          sb.append("  ".repeat(ind + 1));
          str(String.valueOf(e.getKey()), sb);
          sb.append(": ");
          write(e.getValue(), sb, ind + 1);
          sb.append(++i < m.size() ? ",\n" : "\n");
        }
        sb.append("  ".repeat(ind)).append('}');
      } else if (o instanceof Collection<?> c) {
        if (c.isEmpty()) {
          sb.append("[]");
          return;
        }
        boolean simple = c.stream().allMatch(x -> x instanceof String || x instanceof Number);
        if (simple && c.stream().mapToInt(x -> String.valueOf(x).length() + 4).sum() < 100) {
          sb.append('[');
          int i = 0;
          for (Object x : c) {
            write(x, sb, ind);
            if (++i < c.size()) sb.append(", ");
          }
          sb.append(']');
          return;
        }
        sb.append("[\n");
        int i = 0;
        for (Object x : c) {
          sb.append("  ".repeat(ind + 1));
          write(x, sb, ind + 1);
          sb.append(++i < c.size() ? ",\n" : "\n");
        }
        sb.append("  ".repeat(ind)).append(']');
      } else str(String.valueOf(o), sb);
    }

    static void str(String s, StringBuilder sb) {
      sb.append('"');
      for (char ch : s.toCharArray()) {
        switch (ch) {
          case '"' -> sb.append("\\\"");
          case '\\' -> sb.append("\\\\");
          case '\n' -> sb.append("\\n");
          case '\r' -> sb.append("\\r");
          case '\t' -> sb.append("\\t");
          case '<' -> sb.append("\\u003c"); // safe to embed inside <script>
          default -> {
            if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
            else sb.append(ch);
          }
        }
      }
      sb.append('"');
    }
  }

  // ---------------------------------------------------------------------------------------------
  // HTML page (self-contained: no network, no CDN)
  // ---------------------------------------------------------------------------------------------

  static final class Html {
    static final String SVG_STYLE = """
        <style>
        .frcviz{--bg:#ffffff;--fg:#1f2328;--mut:#59636e;--band:#f6f8fa;--bandln:#e1e4e8;--edge:#8c959f;
          --robot:#0969da;--container:#8250df;--command:#bc4c00;--subsystem:#1a7f37;--part:#4b9a5f;
          --other:#57606a;--constants:#bf8700;--lib:#6e7781;--inj:#8250df;}
        @media (prefers-color-scheme:dark){.frcviz{--bg:#0d1117;--fg:#e6edf3;--mut:#9198a1;--band:#151b23;
          --bandln:#262c36;--edge:#6e7681;--robot:#4493f8;--container:#ab7df8;--command:#db6d28;
          --subsystem:#3fb950;--part:#56a868;--other:#9198a1;--constants:#d29922;--lib:#8b949e;--inj:#ab7df8;}}
        :root[data-theme="light"] .frcviz{--bg:#ffffff;--fg:#1f2328;--mut:#59636e;--band:#f6f8fa;--bandln:#e1e4e8;
          --edge:#8c959f;--robot:#0969da;--container:#8250df;--command:#bc4c00;--subsystem:#1a7f37;--part:#4b9a5f;
          --other:#57606a;--constants:#bf8700;--lib:#6e7781;--inj:#8250df;}
        :root[data-theme="dark"] .frcviz{--bg:#0d1117;--fg:#e6edf3;--mut:#9198a1;--band:#151b23;--bandln:#262c36;
          --edge:#6e7681;--robot:#4493f8;--container:#ab7df8;--command:#db6d28;--subsystem:#3fb950;--part:#56a868;
          --other:#9198a1;--constants:#d29922;--lib:#8b949e;--inj:#ab7df8;}
        .frcviz{background:var(--bg)}
        .frcviz .band rect{fill:var(--band);stroke:var(--bandln)}
        .frcviz .band text{fill:var(--mut);font-size:12px;font-weight:600}
        .frcviz .node rect{fill:var(--bg);stroke:var(--c);stroke-width:2}
        .frcviz .node .nm{fill:var(--fg);font-size:13px;font-weight:600;text-anchor:middle}
        .frcviz .node .st{fill:var(--mut);font-size:10.5px;text-anchor:middle}
        .frcviz .node .badge{fill:var(--mut);font-size:9.5px;text-anchor:end}
        .frcviz .node{cursor:pointer;outline:none}
        .frcviz .node:focus rect,.frcviz .node.sel rect{stroke-width:3.5}
        .frcviz .cat-robot{--c:var(--robot)} .frcviz .cat-container{--c:var(--container)}
        .frcviz .cat-command{--c:var(--command)} .frcviz .cat-subsystem{--c:var(--subsystem)}
        .frcviz .cat-subsystem-part{--c:var(--part)} .frcviz .cat-other{--c:var(--other)}
        .frcviz .cat-constants{--c:var(--constants)} .frcviz .cat-library,.frcviz .cat-library-base{--c:var(--lib)}
        .frcviz .cat-library rect,.frcviz .cat-library-base rect{stroke-dasharray:5 3}
        .frcviz .edge .line{fill:none;stroke:var(--edge);stroke-width:1.3}
        .frcviz .edge .hit{fill:none;stroke:transparent;stroke-width:9}
        .frcviz .k-implements .line{stroke-dasharray:6 4}
        .frcviz .k-injected .line{stroke:var(--inj);stroke-dasharray:5 3}
        .frcviz .k-creates .line{stroke-dasharray:2 3}
        .frcviz .k-uses .line{stroke-dasharray:1 4;opacity:.7}
        .frcviz .mk-line{fill:none;stroke:var(--edge);stroke-width:1.5}
        .frcviz .mk-hollow{fill:var(--bg);stroke:var(--edge);stroke-width:1.2}
        .frcviz .mk-fill{fill:var(--edge)}
        .frcviz .dim{opacity:.12}
        .frcviz .edge.hl .line{stroke:var(--fg);stroke-width:2}
        </style>
        """;

    static String page(Map<String, Object> meta, String json, String archSvg, String fullSvg) {
      String title = "Robot code structure";
      String sub = "Team " + (String.valueOf(meta.get("teamNumber")).isEmpty() ? "?" : meta.get("teamNumber"))
          + " · " + meta.get("name") + " · WPILib " + meta.get("wpilibVersion") + " · "
          + meta.get("commandFramework");
      return TEMPLATE.replace("%%TITLE%%", esc(title)).replace("%%SUB%%", esc(sub))
          .replace("%%ARCH%%", archSvg).replace("%%FULL%%", fullSvg)
          .replace("%%DATA%%", json.replace("</", "<\\/"));
    }

    static final String TEMPLATE = """
        <!doctype html>
        <html lang="en">
        <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Robot Code Map</title>
        <meta name="generator" content="SCREAM AI Tools frc-visualize-code">
        <style>
        :root{--bg:#ffffff;--fg:#1f2328;--mut:#59636e;--bd:#d1d9e0;--panel:#f6f8fa;--acc:#0969da;--chip:#eef1f4}
        @media (prefers-color-scheme:dark){:root:not([data-theme="light"]){--bg:#0d1117;--fg:#e6edf3;--mut:#9198a1;
          --bd:#3d444d;--panel:#151b23;--acc:#4493f8;--chip:#212830}}
        :root[data-theme="dark"]{--bg:#0d1117;--fg:#e6edf3;--mut:#9198a1;--bd:#3d444d;--panel:#151b23;--acc:#4493f8;--chip:#212830}
        *{box-sizing:border-box}
        body{margin:0;background:var(--bg);color:var(--fg);font:14px/1.45 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
        header{padding:14px 16px 0;border-bottom:1px solid var(--bd)}
        h1{font-size:18px;margin:0}
        .sub{color:var(--mut);font-size:12.5px;margin:2px 0 10px}
        nav{display:flex;gap:4px;flex-wrap:wrap}
        nav button,.seg button{font:inherit;background:none;border:0;border-bottom:2px solid transparent;color:var(--mut);padding:8px 12px;cursor:pointer}
        nav button[aria-selected="true"]{color:var(--fg);border-bottom-color:var(--acc);font-weight:600}
        .tab{display:none}.tab.on{display:block}
        .bar{display:flex;flex-wrap:wrap;gap:8px 14px;align-items:center;padding:10px 16px;border-bottom:1px solid var(--bd);font-size:12.5px;color:var(--mut)}
        .seg{display:inline-flex;border:1px solid var(--bd);border-radius:8px;overflow:hidden}
        .seg button{border:0;padding:4px 10px;border-radius:0}
        .seg button[aria-pressed="true"]{background:var(--chip);color:var(--fg);font-weight:600}
        .zoom button{font:inherit;min-width:30px;padding:3px 8px;border:1px solid var(--bd);background:var(--bg);color:var(--fg);border-radius:6px;cursor:pointer}
        .legend{display:flex;flex-wrap:wrap;gap:4px 12px}
        .legend span{white-space:nowrap}
        .sw{display:inline-block;width:10px;height:10px;border-radius:3px;border:2px solid;margin-right:4px;vertical-align:-1px}
        .split{display:flex;min-height:calc(100vh - 160px)}
        .canvas{flex:1;overflow:auto;padding:12px 16px;min-width:0}
        .canvas svg{display:block;transform-origin:0 0}
        aside{overflow-wrap:anywhere;width:340px;flex:none;border-left:1px solid var(--bd);padding:14px 16px;background:var(--panel);overflow:auto;max-height:calc(100vh - 160px);position:sticky;top:0}
        aside h2{font-size:16px;margin:0 0 2px}
        aside h3{font-size:12px;text-transform:uppercase;letter-spacing:.04em;color:var(--mut);margin:14px 0 4px}
        aside ul{margin:0;padding-left:18px} aside li{margin:2px 0}
        code{font:12px ui-monospace,SFMono-Regular,Menlo,Consolas,monospace;background:var(--chip);padding:1px 4px;border-radius:4px;overflow-wrap:anywhere}
        .muted{color:var(--mut)}
        .pill{display:inline-block;font-size:11px;padding:1px 7px;border-radius:999px;background:var(--chip);color:var(--mut);margin-right:4px}
        a.cls{color:var(--acc);cursor:pointer;text-decoration:none} a.cls:hover{text-decoration:underline}
        .wrap{padding:14px 16px;max-width:1200px}
        table{border-collapse:collapse;width:100%;font-size:13px}
        th,td{text-align:left;vertical-align:top;padding:7px 8px;border-bottom:1px solid var(--bd)}
        th{font-size:11.5px;text-transform:uppercase;letter-spacing:.04em;color:var(--mut);font-weight:600;position:sticky;top:0;background:var(--bg)}
        h2.sec{font-size:15px;margin:18px 0 6px}
        .note{font-size:12.5px;color:var(--mut);max-width:80ch}
        .warn{border-left:3px solid #bf8700;padding:6px 10px;background:var(--panel);margin:6px 0;font-size:13px}
        @media (max-width:820px){.split{flex-direction:column}aside{width:auto;border-left:0;border-top:1px solid var(--bd);max-height:none;position:static}
          table,thead,tbody,tr,td{display:block}thead{display:none}tr{border-bottom:1px solid var(--bd);padding:6px 0}td{border:0;padding:2px 0}
          td[data-l]::before{content:attr(data-l) ": ";color:var(--mut);font-size:11.5px}}
        </style>
        </head>
        <body>
        <header>
          <h1>%%TITLE%%</h1>
          <p class="sub">%%SUB%%</p>
          <nav role="tablist">
            <button role="tab" aria-selected="true" data-tab="structure">Structure</button>
            <button role="tab" aria-selected="false" data-tab="controls">Controls</button>
            <button role="tab" aria-selected="false" data-tab="about">About this map</button>
          </nav>
        </header>

        <section class="tab on" id="structure">
          <div class="bar">
            <span class="seg" role="group" aria-label="View">
              <button aria-pressed="true" data-view="arch">Architecture</button>
              <button aria-pressed="false" data-view="full">Everything</button>
            </span>
            <span class="zoom"><button data-z="-1" aria-label="Zoom out">−</button> <button data-z="0">Fit</button> <button data-z="1" aria-label="Zoom in">+</button></span>
            <span class="legend">
              <span><i class="sw" style="border-color:#0969da"></i>Robot</span>
              <span><i class="sw" style="border-color:#8250df"></i>Wiring</span>
              <span><i class="sw" style="border-color:#bc4c00"></i>Commands</span>
              <span><i class="sw" style="border-color:#1a7f37"></i>Mechanisms</span>
              <span><i class="sw" style="border-color:#bf8700"></i>Constants</span>
              <span><i class="sw" style="border-color:#6e7781;border-style:dashed"></i>Library</span>
              <span>◁ extends / implements · ◆ creates &amp; stores in a field (<code>= new X()</code>) · ◇ stores a reference in a field · <span style="color:#8250df">╌</span> passed in its constructor · ┈ creates · ⋯ refers to</span>
            </span>
          </div>
          <div class="split">
            <div class="canvas" id="canvas">
              <div data-svg="arch">%%ARCH%%</div>
              <div data-svg="full" hidden>%%FULL%%</div>
            </div>
            <aside id="detail" aria-live="polite"><p class="muted">Select a class to see its fields, command factories, links and the buttons that reach it.</p></aside>
          </div>
        </section>

        <section class="tab" id="controls"><div class="wrap" id="controlsBody"></div></section>
        <section class="tab" id="about"><div class="wrap" id="aboutBody"></div></section>

        <script id="data" type="application/json">%%DATA%%</script>
        <script>
        (function(){
        "use strict";
        var D = JSON.parse(document.getElementById("data").textContent);
        var C = {}; D.classes.forEach(function(c){ C[c.id] = c; });
        var CAT = {"robot":"Entry point","container":"Wiring","command":"Commands","subsystem":"Mechanism",
          "subsystem-part":"Mechanism part","other":"Project code","constants":"Constants",
          "library-base":"Library base class","library":"Library"};
        function el(t,a,h){var e=document.createElement(t);if(a)for(var k in a)e.setAttribute(k,a[k]);if(h!=null)e.innerHTML=h;return e;}
        function x(s){return String(s==null?"":s).replace(/[&<>"]/g,function(c){return{"&":"&amp;","<":"&lt;",">":"&gt;","\\"":"&quot;"}[c];});}
        function short(id){return C[id]?C[id].name:String(id).split(".").pop();}
        function link(id){return C[id]?'<a class="cls" data-go="'+x(id)+'">'+x(C[id].name)+'</a>':'<code>'+x(short(id))+'</code>';}

        // Tabs
        document.querySelectorAll("nav button").forEach(function(b){b.addEventListener("click",function(){
          document.querySelectorAll("nav button").forEach(function(o){o.setAttribute("aria-selected",o===b);});
          document.querySelectorAll(".tab").forEach(function(t){t.classList.toggle("on",t.id===b.dataset.tab);});
        });});

        // Views + zoom
        var view="arch", zoom=1;
        function svg(){return document.querySelector('[data-svg="'+view+'"] svg');}
        function applyZoom(){document.querySelectorAll(".canvas svg").forEach(function(s){
          var w=+s.getAttribute("width"),h=+s.getAttribute("height");s.style.width=(w*zoom)+"px";s.style.height=(h*zoom)+"px";});}
        function fit(){var s=svg();var w=+s.getAttribute("width");var avail=document.getElementById("canvas").clientWidth-32;zoom=Math.min(1,avail/w);applyZoom();}
        document.querySelectorAll(".seg button").forEach(function(b){b.addEventListener("click",function(){
          view=b.dataset.view;document.querySelectorAll(".seg button").forEach(function(o){o.setAttribute("aria-pressed",o===b);});
          document.querySelectorAll("[data-svg]").forEach(function(d){d.hidden=d.dataset.svg!==view;});
          fit();if(selected)select(selected,true);});});
        document.querySelectorAll(".zoom button").forEach(function(b){b.addEventListener("click",function(){
          var z=+b.dataset.z; if(z===0)fit(); else {zoom=Math.max(.2,Math.min(3,zoom*(z>0?1.25:0.8)));applyZoom();}});});
        window.addEventListener("load",fit);

        // Selection
        var selected=null;
        function select(id,keepScroll){
          selected=id;
          document.querySelectorAll(".canvas svg").forEach(function(s){
            var linked={};linked[id]=1;
            s.querySelectorAll(".edge").forEach(function(e){var on=e.dataset.from===id||e.dataset.to===id;
              e.classList.toggle("dim",!on);e.classList.toggle("hl",on);if(on){linked[e.dataset.from]=1;linked[e.dataset.to]=1;}});
            s.querySelectorAll(".node").forEach(function(n){n.classList.toggle("dim",!linked[n.dataset.id]);n.classList.toggle("sel",n.dataset.id===id);});
          });
          detail(id);
          if(!keepScroll){var n=svg().querySelector('.node[data-id="'+CSS.escape(id)+'"]');if(n&&n.scrollIntoView)n.scrollIntoView({block:"nearest",inline:"center",behavior:"smooth"});}
        }
        function clear(){selected=null;document.querySelectorAll(".canvas .dim,.canvas .hl,.canvas .sel").forEach(function(e){e.classList.remove("dim","hl","sel");});
          document.getElementById("detail").innerHTML='<p class="muted">Select a class to see its fields, command factories, links and the buttons that reach it.</p>';}
        document.querySelectorAll(".canvas svg").forEach(function(s){
          s.addEventListener("click",function(ev){var n=ev.target.closest(".node");if(n)select(n.dataset.id,true);else clear();});
          s.addEventListener("keydown",function(ev){var n=ev.target.closest(".node");if(n&&(ev.key==="Enter"||ev.key===" ")){ev.preventDefault();select(n.dataset.id,true);}});
        });
        document.addEventListener("click",function(ev){var a=ev.target.closest("[data-go]");if(!a)return;
          ev.preventDefault();document.querySelector('nav button[data-tab="structure"]').click();
          var id=a.dataset.go; if(!svg().querySelector('.node[data-id="'+CSS.escape(id)+'"]')){
            var full=document.querySelector('.seg button[data-view="full"]'); if(full)full.click();}
          select(id);});

        function edgesFor(id){var v=view==="arch"?D.architectureView:D.fullView;var out=[],inn=[];
          v.edges.forEach(function(e){if(e.from===id)out.push(e);else if(e.to===id)inn.push(e);});return{out:out,inn:inn};}
        var VERB={"extends":"extends","implements":"implements","owns":"creates and stores","has":"stores a reference to","injected":"is passed","creates":"creates","uses":"refers to"};
        var VERB_IN={"extends":"extends it","implements":"implements it","owns":"creates and stores it in a field","has":"stores a reference to it in a field","injected":"is passed one in its constructor","creates":"creates one","uses":"refers to it"};
        function detail(id){
          var c=C[id]; var d=document.getElementById("detail"); if(!c){d.innerHTML="";return;}
          var h='<h2>'+x(c.name)+'</h2><div><span class="pill">'+x(CAT[c.category]||c.category)+'</span>'+(c.kind!=="class"?'<span class="pill">'+x(c.kind)+'</span>':'')+'</div>';
          h+='<p class="muted" style="margin:6px 0 0"><code>'+x(c.file)+':'+c.line+'</code></p>';
          if(c.subsystemByName)h+='<p class="warn">Treated as a mechanism because its superclass is named like one; its source was not found.</p>';
          var sup=c.extends.concat(c.implements); if(sup.length)h+='<h3>Is a</h3><ul>'+sup.map(function(s){return'<li>'+link(s)+(C[s]?'':' <span class="muted">'+x(s)+'</span>')+'</li>';}).join("")+'</ul>';
          var ed=edgesFor(id);
          if(ed.out.length)h+='<h3>Uses</h3><ul>'+ed.out.map(function(e){return'<li>'+x(VERB[e.kind])+' '+link(e.to)+(e.via?' <span class="muted">('+x(e.via.join(", "))+')</span>':'')+'</li>';}).join("")+'</ul>';
          if(ed.inn.length)h+='<h3>Used by</h3><ul>'+ed.inn.map(function(e){return'<li>'+link(e.from)+' <span class="muted">'+x(VERB_IN[e.kind])+'</span></li>';}).join("")+'</ul>';
          var nested=(view==="arch"?D.architectureView:D.fullView).foldedNestedTypes[id];
          if(nested&&nested.length)h+='<h3>Nested types</h3><ul>'+nested.map(function(n){return'<li><code>'+x(n)+'</code></li>';}).join("")+'</ul>';
          var bs=D.bindings.filter(function(b){return b.mechanisms.indexOf(id)>=0||b.declaredIn===id;});
          if(bs.length)h+='<h3>Buttons that reach it</h3><ul>'+bs.map(function(b){return'<li><b>'+x(b.controller)+'</b> '+x(b.trigger)+' <span class="muted">'+x(b.binding)+'</span> → <code>'+x(b.command)+'</code></li>';}).join("")+'</ul>';
          var ds=D.defaultCommands.filter(function(b){return b.mechanism===id;});
          if(ds.length)h+='<h3>Default command</h3><ul>'+ds.map(function(b){return'<li><code>'+x(b.command)+'</code> <span class="muted">('+x(b.file)+':'+b.line+')</span></li>';}).join("")+'</ul>';
          var cf=c.publicMethods.filter(function(m){return m.commandFactory;}), other=c.publicMethods.filter(function(m){return !m.commandFactory;});
          if(cf.length)h+='<h3>Command factories</h3><ul>'+cf.map(function(m){return'<li><code>'+x(m.name)+'()</code></li>';}).join("")+'</ul>';
          if(other.length)h+='<h3>Other public methods</h3><ul>'+other.slice(0,40).map(function(m){return'<li><code>'+x(m.name)+'()</code> <span class="muted">'+x(m.returns)+'</span></li>';}).join("")+(other.length>40?'<li class="muted">… '+(other.length-40)+' more</li>':'')+'</ul>';
          var fs=c.fields.filter(function(f){return !f.static;});
          if(fs.length)h+='<h3>Fields</h3><ul>'+fs.map(function(f){return'<li><code>'+x(f.name)+'</code> <span class="muted">'+(f.typeId?link(f.typeId):x(f.type))+'</span></li>';}).join("")+'</ul>';
          if(c.constantCount)h+='<h3>Constants</h3><p>'+c.constantCount+' static fields</p>';
          d.innerHTML=h;
        }

        // Controls tab
        (function(){
          var b=document.getElementById("controlsBody"),h="";
          h+='<p class="note">Every trigger binding and default command found in the code reachable from <code>'+x(short(D.root))+'</code>. '+
             '“Mechanisms” are the subsystems each command expression refers to — read from the source, following calls a few levels deep. '+
             'That is an estimate, not the command\\'s declared requirements: check the command itself before relying on it.</p>';
          if(D.controllers.length){h+='<h2 class="sec">Controllers</h2><table><thead><tr><th>Field</th><th>Type</th><th>Port</th><th>Declared in</th></tr></thead><tbody>'+
            D.controllers.map(function(c){return'<tr><td data-l="Field"><b>'+x(c.name)+'</b></td><td data-l="Type"><code>'+x(c.type)+'</code></td><td data-l="Port">'+x(c.port)+'</td><td data-l="Declared in">'+link(c.declaredIn)+'</td></tr>';}).join("")+'</tbody></table>';}
          var groups={},order=[];
          D.bindings.forEach(function(r){var k=r.controller+(r.controllerPort!==""?" (port "+r.controllerPort+")":"");if(!groups[k]){groups[k]=[];order.push(k);}groups[k].push(r);});
          if(!order.length)h+='<p>No trigger bindings found.</p>';
          order.forEach(function(k){
            h+='<h2 class="sec">'+x(k)+'</h2><table><thead><tr><th>Trigger</th><th>When</th><th>Command</th><th>Mechanisms</th><th>Where</th></tr></thead><tbody>';
            groups[k].forEach(function(r){
              h+='<tr><td data-l="Trigger"><b>'+x(r.trigger)+'</b>'+(r.triggerHint?'<br><span class="muted">'+x(r.triggerHint)+'</span>':'')+'</td>'+
                 '<td data-l="When"><code>'+x(r.binding)+'</code><br><span class="muted">'+x(r.behavior)+'</span></td>'+
                 '<td data-l="Command"><code>'+x(r.command)+'</code></td>'+
                 '<td data-l="Mechanisms">'+(r.mechanisms.length?r.mechanisms.map(link).join(", "):'<span class="muted">none found</span>')+'</td>'+
                 '<td data-l="Where"><span class="muted">'+x(r.file)+':'+r.line+'</span></td></tr>';});
            h+='</tbody></table>';});
          h+='<h2 class="sec">Default commands</h2>';
          if(!D.defaultCommands.length)h+='<p>No <code>setDefaultCommand</code> calls found.</p>';
          else h+='<table><thead><tr><th>Mechanism</th><th>Command</th><th>Also refers to</th><th>Where</th></tr></thead><tbody>'+
            D.defaultCommands.map(function(r){return'<tr><td data-l="Mechanism">'+(r.mechanism?link(r.mechanism):'<code>'+x(r.mechanismField)+'</code>')+'</td><td data-l="Command"><code>'+x(r.command)+'</code></td><td data-l="Also refers to">'+
              (r.mechanisms.filter(function(m){return m!==r.mechanism;}).map(link).join(", ")||'<span class="muted">—</span>')+'</td><td data-l="Where"><span class="muted">'+x(r.file)+':'+r.line+'</span></td></tr>';}).join("")+'</tbody></table>';
          b.innerHTML=h;
        })();

        // About tab
        (function(){
          var p=D.project,h='<h2 class="sec">Project</h2><table><tbody>';
          [["Team",p.teamNumber||"not found"],["WPILib",p.wpilibVersion],["Namespace",p.wpilibNamespace],["Command framework",p.commandFramework],
           ["Entry point",p.rootFile],["Source files parsed",p.parsedFiles],["Classes reachable from the entry point",D.classes.length],["Generated by",D.tool+" (Java "+p.javaVersionUsed+")"]]
           .forEach(function(r){h+='<tr><th style="position:static;width:260px">'+x(r[0])+'</th><td>'+x(r[1])+'</td></tr>';});
          h+='</tbody></table>';
          if(D.warnings.length)h+='<h2 class="sec">Warnings</h2>'+D.warnings.map(function(w){return'<div class="warn">'+x(w)+'</div>';}).join("");
          h+='<h2 class="sec">How to read the map</h2><ul class="note">'+
            '<li>The map starts at <code>'+x(short(D.root))+'</code> and follows every class it refers to, then every class those refer to, and so on. Code nothing reaches is not shown.</li>'+
            '<li><b>Architecture</b> hides constants, minor “uses” references and library internals. <b>Everything</b> shows every reachable class and reference.</li>'+
            '<li>Nested classes are folded into the class that contains them (the “+N” badge).</li>'+
            '<li>Dashed outlines are library code that is in this repository as source (vendored or included builds). WPILib and vendor jars are not shown.</li>'+
            '<li>The tool reads source only. It never compiles or runs robot code, so anything decided at runtime (reflection, choosers, generated code) is invisible to it.</li></ul>';
          document.getElementById("aboutBody").innerHTML=h;
        })();
        })();
        </script>
        </body>
        </html>
        """;
  }
}
