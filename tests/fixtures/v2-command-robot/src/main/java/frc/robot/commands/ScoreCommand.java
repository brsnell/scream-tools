package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Intake;

public class ScoreCommand extends Command {
  private final Arm arm;
  private final Intake intake;

  public ScoreCommand(Arm arm, Intake intake) {
    this.arm = arm;
    this.intake = intake;
    addRequirements(arm, intake);
  }
}
