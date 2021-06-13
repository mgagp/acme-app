package com.acme.app;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AcmeAwsSecretsManagerPropertiesListener implements ApplicationListener<ApplicationPreparedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcmeAwsSecretsManagerPropertiesListener.class);

    private JsonNode awsSecretsJsonSet = null;

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        awsSecretManagerInit();

        if (this.awsSecretsJsonSet == null) {
            LOGGER.info("no secret manager");
            return;
        }

        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        Properties props = new Properties();

        addSecretProp(props, "Application.secret.key");

        environment.getPropertySources().addFirst(new PropertiesPropertySource("aws.secret.manager", props));
    }

    private void addSecretProp(Properties props, String key) {
        String value = getSecret(key);
        props.put(key, value);
    }

    private String getSecret(String key) {
        LOGGER.debug("getSecret {}", key);
        JsonNode secretNode = this.awsSecretsJsonSet.get(key);
        if (secretNode != null) {
            String secretValue = this.awsSecretsJsonSet.get(key).textValue();
            if (secretValue != null && !secretValue.trim().equals("")) {
                LOGGER.debug("getSecret {}={}", key, secretValue);
                return secretValue;
            }
        }
        return null;
    }

    private void awsSecretManagerInit() {
        try {
            var secretName = System.getProperty("acme.config.secret");
            LOGGER.info("acme.config.secret {}", secretName);

            var region = "ca-central-1";

            var client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
            var getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
            var getSecretValueResult = client.getSecretValue(getSecretValueRequest);
            var secret = getSecretValueResult.getSecretString();

            LOGGER.info("secret {}", secret);

            ObjectMapper objectMapper = new ObjectMapper();
            this.awsSecretsJsonSet = objectMapper.readTree(secret);
        }
        catch (Exception e) {
            LOGGER.warn("", e);
        }
    }

}
