package com.baptechs.zeus.module.fccmanage.impl;

import com.baptechs.zeus.api.station.hardwarecontrol.fccmanage.FCCManageService;
import com.baptechs.zeus.domain.station.hardware.Device;
import com.baptechs.zeus.domain.station.hardware.Dispenser;
import com.baptechs.zeus.domain.station.hardware.FuelTank;
import com.baptechs.zeus.module.fccmanage.IFSFModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by warden on 2016/3/17.
 */

@Service("fccManageService")
public class DefaultFCCManageService implements FCCManageService {
    Logger                          logger                      = LoggerFactory.getLogger(DefaultFCCManageService.class);

    @Autowired
    IFSFModuleService               ifsfModuleService;

    @Override
    public List<Device> getAllOnlineDevice() {
        return null;
    }

    @Override
    public FuelTank getFuelTankInfo(FuelTank fuelTank) {
        return null;
    }

    @Override
    public Dispenser getDispenserInfo(Dispenser dispenser) {
        return null;
    }

    @Override
    public boolean preAuthDispenser(Dispenser dispenser, Double money) {
        return false;
    }

    @Override
    public boolean lockDispenser(Dispenser dispenser) {
        return false;
    }

    @Override
    public boolean lockAllDispenser() {
        return false;
    }

    @Override
    public boolean unlockDispenser(Dispenser dispenser) {
        return false;
    }

    @Override
    public boolean unlockAllDispenser() {
        return false;
    }
}
