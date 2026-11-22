package org.example.robot.mechanisms;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

public class Shooter implements Mechanism {
  public Shooter() {
    setDefaultCommand(idle());
  }

  public Command spinUp() {
    return run(coroutine -> {}).named("Shooter Spin Up");
  }

  public Command idle() {
    return run(coroutine -> {}).named("Shooter Idle");
  }
}
