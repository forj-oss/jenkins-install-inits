# Credentials

This feature is used to create credentials at the launch time. It currently supports the following type of credentials 

- Login Password
- Secret Text
- AWS Credentials
- SSH Private Key

## Envionment variables
The path to the configuration file is provided through and environment variable named **CRED_PATH**

## Configuration file
To identify which kind of credential we have to create we use the field type which can have the following values :

- secret_text : Secret text credentials
- user_password : Login password credentials
- aws_credentials : AWS Credentials
- ssh_credentials : SSH username with private key

```json
        [
            {
                "id" : "my key",
                "value" : "my value",
                "domain" : "global",
                "folder" : "",
                "type" : "secret_text"
            },
            {
                "id": "my id2",
                "key" : "my key2",
                "value" : "my value2",
                "domain" : "global",
                "folder" : "",
                "type" : "user_password"
            },
            {
                "id": "my id3",
                "key" : "aws access key",
                "value" : "aws secret access key",
                "domain" : "global",
                "folder" : "",
                "type" : "aws_credentials"
            },
            {
                "id": "lgil3-ssh-key",
                "key" : "lgil3",
                "value" : "-----BEGIN RSA PRIVATE KEY-----
                MIIEpAIBAAKCAQEA9NinCryFwo7tE7hWQsi573V3x7Ql2fnPnSLx8QJMy7/ZnYi/
                jzHQWqh9O0hq6fCgqegBYvKC0XPgXV1oblrlvZI/FlYXuMhxxmG3xvTdexD53++T
                zzR0P4pRYRKtN06mFem4Nniy3zSwLWJWevTSOrAmP8+roIqHhb5bHr0pBxbWBF+Z
                xkbiWUt2wmNWnAZ+8It6XmzCQETJBsGAubK+hIMAqqPNwTO9p/uLtabW00cP3ZQB
                sBFsgdY9JzcF2aH4kPHfGr1q4e8wmWyht+ygL4s6RM1cMidY2TB3aHoUUKXxW+m9
                GmaZ6pVap7Tw1FmimDjBr2ITwo/GH3j1HxjddwIDAQABAoIBAQCqgeG56VVhCvVw
                gTOZKH3Lk0tJyFt/s6bwL/C6vLZMQZtDSILWLoOBTsp/KUxDA+5uxAGLLYUKZZ6+
                hhj+mNBatui2HlhAt138H/rCU62VUVohp46Qgl/eC+3mIwJ7fIrOwccmL+Go7OEv
                icbds3ZfpmHMmEtV7oK4EbsU6fgTVWgVobFGIoDwvTKGmL6OY7HMEFvx9OK2jpbE
                b/PG7mAsKyf3A0UJx0hGZpIJ9UmPkWdW2au5BprvRfsYOSf3hctYx2ANaQnJqMv1
                qsHAcugi/DdB60qk9lFbfNP7d9iKA0Pgd3ieQdbpCeriL2TOmGCXjY596YLQLuXx
                SRmJl9hBAoGBAP1FCTO5kerkfVVvt/214ZsEL3iInUn+qPD9PR0Dsp142JUu5oCu
                tWUSxSR3rtscbKLIIU3Odoju5G0DnP385k2zwtc3jVKzuj/SVgs+gwvL1rgh0Vn6
                l2zuQ2yUikzDEs9O18nZgGvZxy0V/4AMJQ2EULfdIyji//q63rUmIwShAoGBAPd8
                XrwHbJtm1mE/OHgeiEjz3oopC/7Klxud++jxNITO9IAUXCHHpc6E1Cv+jH8CsLjR
                exoiPnocG5U9rXiktChLaCOwIz+JXtaL2cUkWDocAX/XeyIdh6m38ro4skNQ6ASq
                b3HLOGqBgGUn+RrWXiEAJltTk/R+rjlSYL1Ha5MXAoGAKuCAIVCeQmzyRv+F357m
                Fc9MlhRSxI8nXL0SlpHksEIgMIljHo/1R6o40+arJI8YWSqWcMyuRppi8420C0/A
                UZFIQBU4iLLCqkFarcaket/EdyHKhnglIJzprz0qeLphudT4NUTsN9YDoT9jJPNp
                ZnNrEuWOKULc4T63q2VMcmECgYEAmQx3xY5N0vjQ4Qjo0hgHrh3qZMSnn2UH99NG
                ozLviKY3vDFbHKTL3CB22dIsbqtqQJGDsJC21Gsnf67J5Y7tvvGLMG7QDY3VcZbB
                ENK+IQmKv9WxV1Ktv/57BEDltQ2GvsPr8ZJ8jBkUHvqOadNiGMRv9BCGsi/ZbKeO
                Hd/aHfcCgYBGYROzb8sw8E88qP2isePrPEGnEbYxxvl24aOgPhnenbWT05duAZL9
                uDRIXT1anvwy00WqXii65edj30QwrJ89APCo69RXB2aCazfwUXqT5Eo15p/RY72s
                uGdgjhjbL/yGo8PAfpY4nBszlGe7T2h52yhhPAHg4q/kI21Y1N0vBg==
                -----END RSA PRIVATE KEY-----",
                "domain" : "global",
                "folder" : "",
                "type" : "ssh_credentials"
            }
        ]
```