package org.example.robot;

import org.example.robot.commands.Superstructure;
import org.example.robot.mechanisms.Elevator;
import org.example.robot.mechanisms.Shooter;
import org.wpilib.command3.button.CommandGamepad;

public class RobotContainer {
  private static final int kDriverPort = 0;

  private final Elevator elevator = new Elevator();
  private final Shooter shooter = new Shooter();
  private final Superstructure superstructure = new Superstructure(elevator, shooter);
  private final CommandGamepad driver = new CommandGamepad(kDriverPort);

  public RobotContainer() {
    driver.faceDown().onTrue(elevator.toHeight(0.5));
    driver.rightTrigger().whileTrue(superstructure.scoreHigh());
    driver.start().and(driver.back().negate()).onTrue(elevator.zeroHere());
  }
}
