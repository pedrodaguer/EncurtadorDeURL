package br.com.daguer.UrlShortener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UrlData {

    private String originalUrl;
    private long expirationTime;

}
