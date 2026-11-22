package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ArmConstants;

public class Arm extends SubsystemBase {
  private final int motorPort = ArmConstants.kMotorPort;

  public Command toAngle(double degrees) {
    return runOnce(() -> {});
  }
}
