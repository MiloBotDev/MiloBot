package io.github.milobotdev.milobot.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class ImgurFunctions {

    public static String uploadImageToImgur(byte[] imageBytes) throws IOException {
        // Set the client ID and API key for the Imgur API
        Config config = Config.getInstance();

        // Set the endpoint URL for the Imgur API
        String url = "https://api.imgur.com/3/image";

        // Set the request headers
        MediaType mediaType = MediaType.parse("image/png");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.png",
                        RequestBody.create(mediaType, imageBytes))
                .build();

        // Set the request
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Client-ID " + config.getImgurClientId() + " Bearer " + config.getImgurClientSecret())
                .post(requestBody)
                .build();

        // Send the request and get the response
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {
            // Parse the JSON response
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(response.body().string(), JsonObject.class);
            JsonObject data = jsonResponse.getAsJsonObject("data");
            return data.get("link").getAsString();
        }
    }
}
