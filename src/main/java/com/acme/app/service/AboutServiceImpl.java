package com.acme.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AboutServiceImpl implements AboutService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AboutServiceImpl.class);

    private final String applicationVersion = "1.0";

    @Override
    // @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public About about() {
        About about = new About();
        String version = this.applicationVersion;
        about.setApplicationVersion(version);
        LOGGER.info("about version {}", version);
        return about;
    }

}
