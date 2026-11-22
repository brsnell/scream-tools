package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Intake;

public final class Autos {
  private Autos() {}

  public static Command scoreAndBackUp(Drivetrain drivetrain, Arm arm, Intake intake) {
    return Commands.sequence(new ScoreCommand(arm, intake), drivetrain.driveDistance(-1.0));
  }
}
