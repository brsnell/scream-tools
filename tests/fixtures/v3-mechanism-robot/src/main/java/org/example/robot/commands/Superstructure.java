package org.example.robot.commands;

import org.example.robot.mechanisms.Elevator;
import org.example.robot.mechanisms.Shooter;
import org.wpilib.command3.Command;

public class Superstructure {
  private final Elevator elevator;
  private final Shooter shooter;

  public Superstructure(Elevator elevator, Shooter shooter) {
    this.elevator = elevator;
    this.shooter = shooter;
  }

  public Command scoreHigh() {
    return Command.parallel(elevator.toHeight(1.2), shooter.spinUp()).named("Score High");
  }
}
