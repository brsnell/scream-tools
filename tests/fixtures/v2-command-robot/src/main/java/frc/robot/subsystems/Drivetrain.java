package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.DoubleSupplier;

public class Drivetrain extends SubsystemBase {
  public Command arcadeDrive(DoubleSupplier forward, DoubleSupplier turn) {
    return run(() -> {});
  }

  public Command driveDistance(double meters) {
    return run(() -> {}).withTimeout(2.0);
  }
}
