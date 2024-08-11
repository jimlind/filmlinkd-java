package jimlind.filmlinkd;

import java.net.URL;
import java.util.Properties;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

public class Config {
    private static final String BOT_TOKEN_NAME = "discordBotToken";

    private static Properties publicProperties = new Properties();
    private static Properties secretProperties = new Properties();
    private static SecretManagerServiceClient secretManagerServiceClient;

    static {
        URL environmentUrl = ClassLoader.getSystemResource("environment.properties");
        try {
            publicProperties.load(environmentUrl.openStream());
        } catch (Exception e) {
            System.out.printf("Error Loading Properties: %s\n", e);
            System.exit(1);
        }

        try {
            secretManagerServiceClient = SecretManagerServiceClient.create();
        } catch (Exception e) {
            System.out.printf("Error Creating Secret Manager Service Client: %s\n", e);
            System.exit(1);
        }

        secretProperties.setProperty(BOT_TOKEN_NAME,
                getSecret(publicProperties.getProperty("discordBotTokenSecretName"),
                        publicProperties.getProperty("discordBotTokenSecretVersion")));
    }

    public String getBotToken() {
        return secretProperties.getProperty(BOT_TOKEN_NAME);
    }

    private static String getSecret(String secretName, String secretVersion) {
        String project = publicProperties.getProperty("googleCloudProjectId");
        SecretVersionName secretVersionName = SecretVersionName.of(project, secretName, secretVersion);

        AccessSecretVersionResponse secretResponse = secretManagerServiceClient.accessSecretVersion(secretVersionName);
        return secretResponse.getPayload().getData().toStringUtf8();
    }
}