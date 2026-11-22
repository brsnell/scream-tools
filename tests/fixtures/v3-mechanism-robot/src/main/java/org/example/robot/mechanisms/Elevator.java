package org.example.robot.mechanisms;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

public class Elevator implements Mechanism {
  public Command toHeight(double meters) {
    return run(coroutine -> {}).named("Elevator To Height");
  }

  public Command zeroHere() {
    return run(coroutine -> {}).named("Elevator Zero");
  }
}
