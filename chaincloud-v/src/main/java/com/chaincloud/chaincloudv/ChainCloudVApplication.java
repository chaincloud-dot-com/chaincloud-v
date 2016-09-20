package com.chaincloud.chaincloudv;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;

import com.chaincloud.chaincloudv.api.Api;
import com.chaincloud.chaincloudv.preference.Preference_;
import com.chaincloud.chaincloudv.service.AddressService;
import com.chaincloud.chaincloudv.service.AddressService_;
import com.chaincloud.chaincloudv.service.SMSServiceImpl_;
import com.chaincloud.chaincloudv.service.WorkService_;
import com.chaincloud.chaincloudv.util.UncaughtExceptionHandler;
import com.chaincloud.chaincloudv.util.crypto.LinuxSecureRandom;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EApplication;
import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

/**
 * Created by songchenwen on 15/7/24.
 */

@EApplication
public class ChainCloudVApplication extends Application {

    public static Context mContext;


    @AfterInject
    void initContext() {
        mContext = getApplicationContext();

        initCoinType();

        initLog();
        initVWebDomain();
        initWakeLock();

        WorkService_.intent(getApplicationContext()).start();
        SMSServiceImpl_.intent(getApplicationContext()).start();
        AddressService_.intent(getApplicationContext()).start();
    }

    @AfterInject
    void initIcons() {
        Iconify.with(new FontAwesomeModule());
    }

    @AfterInject
    void registerUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @AfterInject
    void initLinuxSecureRandom() {
        new LinuxSecureRandom();
    }


    private void initLog(){
        final File logDir = getLogDir();
        final File logFile = new File(logDir, "chaincloud-v.log");
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
        filePattern.setContext(context);
        filePattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %logger{0} - %msg%n");
        filePattern.start();

        final RollingFileAppender<ILoggingEvent> fileAppender = new
                RollingFileAppender<ILoggingEvent>();
        fileAppender.setContext(context);
        fileAppender.setFile(logFile.getAbsolutePath());

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new
                TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/chaincloud-v.%d.log.gz");
        rollingPolicy.setMaxHistory(7);
        rollingPolicy.start();

        fileAppender.setEncoder(filePattern);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
        logcatTagPattern.setContext(context);
        logcatTagPattern.setPattern("%logger{0}");
        logcatTagPattern.start();

        final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
        logcatPattern.setContext(context);
        logcatPattern.setPattern("[%thread] %msg%n");
        logcatPattern.start();

        final LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(context);
        logcatAppender.setTagEncoder(logcatTagPattern);
        logcatAppender.setEncoder(logcatPattern);
        logcatAppender.start();

        final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
        log.addAppender(fileAppender);
        log.addAppender(logcatAppender);
        log.setLevel(Level.INFO);
    }

    private void initVWebDomain(){
        String domain = new Preference_(mContext).vwebDomain().get();

        Api.setVWebDomain(domain);
    }

    private void initWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();
    }

    public void initCoinType() {
        GlobalParams.coinCode = new Preference_(mContext).coinCode().get();
    }


    public File getLogDir() {
        final File logDir = mContext.getDir("log", Context.MODE_WORLD_READABLE);
        return logDir;
    }
}
