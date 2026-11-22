package org.example.robot;

import org.wpilib.command3.Scheduler;
import org.wpilib.framework.TimedRobot;

public class Robot extends TimedRobot {
  private final RobotContainer container = new RobotContainer();

  @Override
  public void robotPeriodic() {
    Scheduler.getDefault().run();
  }
}
