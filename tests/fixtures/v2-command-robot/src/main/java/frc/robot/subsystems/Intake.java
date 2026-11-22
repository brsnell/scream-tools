package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Intake extends SubsystemBase {
  private final RollerIO io = new RollerIO();

  public Command run() {
    return startEnd(() -> io.setVolts(6.0), () -> io.setVolts(0.0));
  }

  public boolean hasPiece() {
    return false;
  }

  /** Hardware layer, kept in the subsystems package. */
  public static class RollerIO {
    public void setVolts(double volts) {}
  }
}
