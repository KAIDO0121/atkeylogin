package ATKeyLogin.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RescueEmailTemplate {

    private String language;

    public String getSubject() {
        if (this.language.equals("en-US")) {
            return "Please verify your email to rescue your account";
        } else if (this.language.equals("ja-JP")) {
            return "電子メールを確認して、アカウントを回復してください。";
        } else {
            return "請驗證您的電子郵件以恢復您的帳戶";
        }
    }

    public String getBody() {
        if (this.language.equals("en-US")) {
            return """
                <h3>Your verification code is %d</h3>
                <p>Please check</p><p>%s</p>
                <p>to continue rescue process, thank you!</p>
                <p>Sincerely, </p>
                <p>AuthenTrend, ATKey.login</p>
            """;
        } else if (this.language.equals("ja-JP")) {
            return """
                <h3>あなたの認証コードは %d</h3>
                <p>以下のリンクをクリックしてください</p><p>%s</p>
                <p>アカウントの復旧手続きを続けてください、ありがとうございます！</p>
                <p>誠にありがとうございます。</p>
                <p>AuthenTrend, ATKey.login</p>
            """;
        } else {
            return """
                <h3>您的驗證碼為 %d</h3>
                <p>請點擊進入以下連結</p><p>%s</p>
                <p>並繼續帳戶恢復步驟，謝謝！</p>
                <p>誠摯感謝您的支持，</p>
                <p>AuthenTrend, ATKey.login</p>
            """;
        }
    }
}
