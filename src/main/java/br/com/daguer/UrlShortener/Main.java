package br.com.daguer.UrlShortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Classe principal que implementa o manipulador de requisições AWS Lambda.
 * Esta classe é responsável por processar a requisição, gerar um código de URL encurtada,
 * e salvar os dados da URL no Amazon S3.
 */

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();

    /**
     * Metodo que manipula a requisição recebida pelo AWS Lambda.
     *
     * @param input   O mapa de entrada contendo os dados da requisição.
     * @param context O contexto da execução do Lambda.
     * @return Um mapa contendo o código da URL encurtada.
     */
    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {

        String body = (String) input.get("body");

        Map<String, String> bodyMap;
        try {
            // Converte o corpo da requisição JSON em um mapa
            bodyMap = objectMapper.readValue(body, Map.class);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Invalid JSON body: " + exception.getMessage(), exception);
        }

        String originalUrl = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        long expirationTimeInSeconds = Long.parseLong(expirationTime);

        // Gera um código único para a URL encurtada
        String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

        UrlData urlData = new UrlData(originalUrl, expirationTimeInSeconds);

        try {
            // Converte os dados da URL em JSON
            String urlDataJson = objectMapper.writeValueAsString(urlData);

            // Cria a requisição para salvar os dados no S3
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket("daguer-url-shortener-storage")
                    .key(shortUrlCode + ".json")
                    .build();

            // Salva os dados da URL no S3
            s3Client.putObject(request, RequestBody.fromString(urlDataJson));

        } catch (Exception exception) {
            throw new RuntimeException("Error saving URL data: " + exception.getMessage(), exception);
        }

        // Cria a resposta contendo o código da URL encurtada
        Map<String, String> response = new HashMap<>();
        response.put("code", shortUrlCode);

        return response;
    }
}