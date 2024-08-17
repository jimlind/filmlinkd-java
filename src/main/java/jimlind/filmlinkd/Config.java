package jimlind.filmlinkd;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

@Component
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String GCP_PROJECT_ID_NAME = "googleCloudProjectId";
    private static final String DISCORD_BOT_TOKEN_NAME = "discordBotToken";

    private static Properties publicProperties = new Properties();
    private static Properties secretProperties = new Properties();
    private static SecretManagerServiceClient secretManagerServiceClient;

    static {
        try {
            InputStream stream = Config.class.getClassLoader().getResourceAsStream("environment.properties");
            publicProperties.load(stream);
        } catch (Exception e) {
            logger.error("Error Loading Properties", e);
            System.exit(1);
        }

        try {
            secretManagerServiceClient = SecretManagerServiceClient.create();
        } catch (Exception e) {
            logger.error("Error Creating Secret Manager Service Client", e);
            System.exit(1);
        }

        secretProperties.setProperty(DISCORD_BOT_TOKEN_NAME,
                getSecret(publicProperties.getProperty("discordBotTokenSecretName"),
                        publicProperties.getProperty("discordBotTokenSecretVersion")));
    }

    public String getGoogleProjectId() {
        return publicProperties.getProperty(GCP_PROJECT_ID_NAME);
    }

    public String getDiscordBotToken() {
        return secretProperties.getProperty(DISCORD_BOT_TOKEN_NAME);
    }

    private static String getSecret(String secretName, String secretVersion) {
        String project = publicProperties.getProperty(GCP_PROJECT_ID_NAME);
        SecretVersionName secretVersionName = SecretVersionName.of(project, secretName, secretVersion);

        AccessSecretVersionResponse secretResponse = secretManagerServiceClient.accessSecretVersion(secretVersionName);
        return secretResponse.getPayload().getData().toStringUtf8();
    }
}