package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.Autos;
import frc.robot.commands.ScoreCommand;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;

public class RobotContainer {
  private final Drivetrain drivetrain = new Drivetrain();
  private final Intake intake = new Intake();
  private final Arm arm = new Arm();

  private final CommandXboxController driver =
      new CommandXboxController(OperatorConstants.kDriverControllerPort);
  private final Joystick buttonBoard = new Joystick(OperatorConstants.kOperatorControllerPort);

  public RobotContainer() {
    drivetrain.setDefaultCommand(
        drivetrain.arcadeDrive(() -> -driver.getLeftY(), () -> -driver.getRightX()));
    configureBindings();
  }

  private void configureBindings() {
    driver.a().onTrue(arm.toAngle(30.0));
    driver.rightBumper().whileTrue(intake.run());
    driver.b().and(driver.leftBumper()).toggleOnTrue(new ScoreCommand(arm, intake));
    new JoystickButton(buttonBoard, 3).onTrue(Autos.scoreAndBackUp(drivetrain, arm, intake));
    new Trigger(intake::hasPiece).onTrue(arm.toAngle(0.0));
  }

  public Command getAutonomousCommand() {
    return Autos.scoreAndBackUp(drivetrain, arm, intake);
  }
}
