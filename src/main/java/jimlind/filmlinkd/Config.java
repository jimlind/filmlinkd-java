package jimlind.filmlinkd;

import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Config {
    private static final String GCP_PROJECT_ID_NAME = "googleCloudProjectId";
    private static final String DISCORD_BOT_TOKEN_NAME = "discordBotToken";
    private static final String PUBSUB_LOG_ENTRY_TOPIC_NAME = "logEntryTopicName";
    private static final String PUBSUB_LOG_ENTRY_SUBSCRIPTION_NAME = "logEntrySubscriptionName";
    private static final String FIRESTORE_COLLECTION_ID = "firesStoreCollectionId";

    private static Properties publicProperties = new Properties();
    private static Properties secretProperties = new Properties();
    private static SecretManagerServiceClient secretManagerServiceClient;

    static {
        String environment = System.getenv("FILMLINKD_ENVIRONMENT");
        environment = environment != null ? environment : "";

        try {
            String resourcesDir = environment.equals("PRODUCTION") ? "prod/" : "dev/";
            InputStream stream = Config.class.getClassLoader()
                    .getResourceAsStream(resourcesDir + "environment.properties");

            publicProperties.load(stream);
        } catch (Exception e) {
            log.error("Error Loading Properties", e);
            System.exit(1);
        }

        try {
            secretManagerServiceClient = SecretManagerServiceClient.create();
        } catch (Exception e) {
            log.error("Error Creating Secret Manager Service Client", e);
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

    public String getPubSubLogEntryTopicName() {
        return publicProperties.getProperty(PUBSUB_LOG_ENTRY_TOPIC_NAME);
    }

    public String getPubSubLogEntrySubscriptionName() {
        return publicProperties.getProperty(PUBSUB_LOG_ENTRY_SUBSCRIPTION_NAME);
    }

    public String getFirestoreCollectionId() {return publicProperties.getProperty(FIRESTORE_COLLECTION_ID);

    private static String getSecret(String secretName, String secretVersion) {
        String project = publicProperties.getProperty(GCP_PROJECT_ID_NAME);
        SecretVersionName secretVersionName = SecretVersionName.of(project, secretName, secretVersion);

        AccessSecretVersionResponse secretResponse = secretManagerServiceClient.accessSecretVersion(secretVersionName);
        return secretResponse.getPayload().getData().toStringUtf8();
    }
}