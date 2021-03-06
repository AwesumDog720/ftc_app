package com.hfrobots.tnt.season1516;


import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.util.Range;

/**
 * Provide a basic manual operational mode that controls the tank drive.
 */
public class TankbotTeleop extends TankbotTelemetry

{
    protected boolean useEncoders = false;

    /*
     * Construct the class.
     *
     * The system calls this member when the class is instantiated.
     */
    public TankbotTeleop() {
    }

    /**
     * Places the robot into the initial pre-running state.
     * Robot must fit within sizing guidelines (18x18)
     * before this state completes.
     */
    @Override
    public void init() {
        super.init();

        if (!useEncoders) {
            runWithoutDriveEncoders();
        } else {
            runUsingEncoders();
        }

        setHookServoPosition(0);
        hookState = HOOK_STATE.DRIVING;
    }


    /**
     * Implement a state machine that controls the robot during
     * manual-operation.  The state machine uses gamepad input to transition
     * between states.
     *
     * The system calls this member repeatedly while the OpMode is running.
     */
    @Override public void loop ()

    {


        steeringPriorityDrive();

        if (gamepad1.a) {
            if (hookDebounceTime == 0) {
                hookDebounceTime = System.currentTimeMillis();
            } else {
                long now = System.currentTimeMillis();
                long elapsedTime = now - hookDebounceTime;

                if (elapsedTime > 1000) {
                    // 1 second
                    hookDebounceTime = System.currentTimeMillis();
                } else {
                    return; // button might be bounced, don't process
                }
            }
            
            switch (hookState) {
                case DRIVING:
                    setHookServoPosition(1.0);
                    hookState = HOOK_STATE.PICKUP;
                    break;
                case PICKUP:
                    setHookServoPosition(0.5);
                    hookState = HOOK_STATE.CARRYING;
                    break;
                case CARRYING:
                    setHookServoPosition(1.0);
                    hookState = HOOK_STATE.DROP_OFF;
                    break;
                case DROP_OFF:
                    setHookServoPosition(0.0);
                    hookState = HOOK_STATE.DRIVING;
                    break;
            }
        }

        //
        // Send telemetry data to the driver station.
        //
        updateTelemetry(); // Update common telemetry
        updateGamepadTelemetry();

    }

    private void steeringPriorityDrive() {
        //----------------------------------------------------------------------
        //
        // DC Motors
        //
        // Obtain the current values of the joystick controllers.
        //
        // Note that x and y equal -1 when the joystick is pushed all of the way
        // forward (i.e. away from the human holder's body).
        //
        // The clip method guarantees the value never exceeds the range +-1.
        //
        // The DC motors are scaled to make it easier to control them at slower
        // speeds.
        //
        // The setPower methods write the motor power values to the DcMotor
        // class, but the power levels aren't applied until the loop() method ends.
        //

        float xValue = -scaleMotorPower(gamepad1.left_stick_x);
        float yValue = -scaleMotorPower(gamepad1.left_stick_y);

        //calculate the power needed for each motor
        float leftPower = yValue + xValue;
        float rightPower = yValue - xValue;


        //clip the power values so that it only goes from -1 to 1
        leftPower = Range.clip(leftPower, -1, 1);
        rightPower = Range.clip(rightPower, -1, 1);

        //set the power of the motors with the gamepad values
        setDrivePower(leftPower, rightPower);
    }

    private enum HOOK_STATE {
        DRIVING, PICKUP, CARRYING, DROP_OFF;
    }

    private HOOK_STATE hookState;

    private long hookDebounceTime = 0L;
}
