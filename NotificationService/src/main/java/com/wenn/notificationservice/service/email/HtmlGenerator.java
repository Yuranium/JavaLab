package com.wenn.notificationservice.service.email;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class HtmlGenerator {
    public String generateVerificationHtml(String username, String email, Integer code) {
        return """
            <!doctype html>
            <html lang="ru">
            <head>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Код подтверждения</title>
              <style>
                body { font-family: Arial, sans-serif; background:#f4f6f8; margin:0; padding:0;}
                .card { max-width:600px; margin:48px auto; background:#fff; border-radius:12px; box-shadow:0 8px 30px rgba(0,0,0,0.08); padding:32px;}
                .logo { font-weight:700; color:#2b6cb0; font-size:20px; margin-bottom:12px;}
                .title { font-size:18px; margin:8px 0 16px; color:#111827;}
                .code { display:inline-block; font-size:28px; letter-spacing:6px; background:#eef2ff; padding:12px 20px; border-radius:8px; color:#2d3748; font-weight:600;}
                .note { color:#4a5568; margin-top:16px; font-size:14px;}
                .footer { margin-top:22px; color:#94a3b8; font-size:12px;}
              </style>
            </head>
            <body>
              <div class="card">
                <div class="logo">JavaLab</div>
                <div class="title">Здравствуйте, %s</div>
                <div style="text-align:center; margin:22px 0;">
                  <div class="code">%s</div>
                </div>
                <div class="note">Код подтверждения отправлен на %s. Если вы не запрашивали этот код — проигнорируйте это письмо.</div>
                <div class="footer">© %d JavaLab</div>
              </div>
            </body>
            </html>
            """.formatted(escapeHtml(username), code.toString(), escapeHtml(email), java.time.Year.now().getValue());
    }

    public String generateTaskCreatedHtml(String title,
                                          String difficulty,
                                          Collection<String> categories) {

        String categoriesHtml = categories.stream()
                .map(this::escapeHtml)
                .map(cat -> "<li>" + cat + "</li>")
                .collect(Collectors.joining());

        return """
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8"/>
                <style>
                    body { font-family: Arial; background:#f4f6f8; padding:20px;}
                    .card {
                        background:white;
                        border-radius:12px;
                        padding:30px;
                        max-width:600px;
                        margin:auto;
                        box-shadow:0 10px 25px rgba(0,0,0,0.08);
                    }
                    .title { font-size:22px; font-weight:600; margin-bottom:10px;}
                    .difficulty { color:#2563eb; font-weight:500;}
                    .footer { margin-top:20px; color:#94a3b8; font-size:12px; }
                    ul { padding-left:20px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="title">Новая задача добавлена</div>
                    
                    <p><b>Название:</b> %s</p>
                    
                    <p class="difficulty">
                        <b>Сложность:</b> %s
                    </p>

                    <p><b>Категории:</b></p>
                    <ul>
                        %s
                    </ul>

                    <p>Перейдите в систему чтобы решить её.</p>
                    <div class="footer">© %d JavaLab</div>
                </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(title),
                escapeHtml(difficulty),
                categoriesHtml,
                java.time.Year.now().getValue()
        );
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

}
