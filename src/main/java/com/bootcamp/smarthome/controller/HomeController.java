package com.bootcamp.smarthome.controller;

import com.bootcamp.smarthome.device.Device;
import com.bootcamp.smarthome.exceptions.DeviceNotFoundException;
import com.bootcamp.smarthome.exceptions.DeviceOfflineException;
import com.bootcamp.smarthome.exceptions.HomeAutomationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central hub that manages all registered smart devices.
 *
 * Devices are stored in a fixed-size array (maximum {@value #MAX_DEVICES}).
 * The controller routes commands to devices by their ID.
 */
public class HomeController {

    public static final int MAX_DEVICES = 8;

    private final Device[] devices = new Device[MAX_DEVICES];
    private int deviceCount = 0;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    // -------------------------------------------------------------------------
    // Device registration
    // -------------------------------------------------------------------------

    /**
     * Registers a new device with the controller.
     * The controller accepts at most {@value #MAX_DEVICES} devices.
     *
     * @param device the device to register
     * @throws IllegalStateException if the device limit has been reached
     */
    public void addDevice(Device device) {
        if (deviceCount >= MAX_DEVICES) {
            throw new IllegalStateException(
                    "Cannot add device '" + device.getDeviceId() +
                    "': controller is at maximum capacity (" + MAX_DEVICES + ").");
        }
        devices[deviceCount] = device;
        deviceCount++;
        System.out.println("Device registered: " + device);
    }

    // -------------------------------------------------------------------------
    // Device lookup
    // -------------------------------------------------------------------------

    /**
     * Finds a registered device by its ID.
     *
     * Returns {@code null} when no matching device is found.
     */
    public Device findDevice(String deviceId) {
        for (int i = 0; i < deviceCount; i++) {
            if (devices[i] != null && devices[i].getDeviceId().equals(deviceId)) {
                return devices[i];
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Command routing
    // -------------------------------------------------------------------------

    /**
     * Parses {@code fullCommand}, resolves the target device, and delegates
     * execution to {@link Device#executeCommand(String)}.
     *
     * Full command format: {@code "DEVICE_ID ACTION [VALUE]"}
     * Example: {@code "LIGHT_01 SET_BRIGHTNESS 75"}
     *
     * @param fullCommand the full command string
     */
    public void sendCommand(String fullCommand) throws HomeAutomationException{
        String deviceId = CommandParser.extractDeviceId(fullCommand);
        String command  = CommandParser.extractCommand(fullCommand);
        logger.debug("The command '{}' for the device '{}' is recieved", command, deviceId);
        try{
            Device device = findDevice(deviceId);

            if (device == null) {
                logger.warn("Device not found: {}", deviceId);
                throw new DeviceNotFoundException("No device found with " + deviceId + " id");
            }

            if (!device.isOnline()) {
                logger.warn("Device '{}' is offline — command skipped.", deviceId);
                throw new DeviceOfflineException("Device " + deviceId + " is offline");
            }

            device.executeCommand(command);
        }
        catch (HomeAutomationException ex){
            logger.error("Device '{}': exception occurred '{}'", deviceId, ex.getMessage());
            throw new HomeAutomationException(String.format("Command '%s' failed for device '%s': '%s'", fullCommand, deviceId, ex.getMessage()), ex);
        }
        finally {
            logger.info("Command processing ended for device '{}'", deviceId);
        }

    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /** Prints the status of every registered device. */
    public void printAllDevices() {
        System.out.println("=== Registered Devices (" + deviceCount + "/" + MAX_DEVICES + ") ===");
        for (int i = 0; i < deviceCount; i++) {
            System.out.println("  " + devices[i]);
        }
    }

    public int getDeviceCount() {
        return deviceCount;
    }
}
