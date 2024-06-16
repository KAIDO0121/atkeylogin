package ATKeyLogin.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignUpEmailTemplate {

    private String language;

    public String getSubject() {
        
        if (this.language.equals("en-US")) {
            return "Please verify your email to get started";
        } else if (this.language.equals("ja-JP")) {
            return "電子メールを確認して、アカウントを登録してください。";
        } else {
            return "請驗證您的電子郵件以註冊您的帳戶";
        }
    }

    public String getBody() {
        if (this.language.equals("en-US")) {
            return """
                <h3>Hi %s, Welcome to ATKey.login!</h3><p>Please enter the code below to continue with your ATKey.login account:</p>
                <p>Email Verification Code: <strong>[%d]</strong></p><p>Please note the code will expire in 48 hours.</p>
                <p>Please contact our customer service team if you have any questions.</p>
                <p>Redirect URL: </p><p>%s</p><p>Sincerely, </p><p>AuthenTrend, ATKey.login</p>
            """;
        } else if (this.language.equals("ja-JP")) {
            return """
                <h3>親愛なる %s 様、こんにちは。ATKey.loginへようこそ！</h3><p>ATKey.loginアカウントの登録を続けるために、以下の認証コードを入力してください：</p>
                <p>認証コード：<strong>[%d]</strong></p><p>この認証コードは48時間以内に無効になりますので、ご注意ください。</p>
                <p>何かご質問があれば、お気軽に弊社のカスタマーサービスチームにお問い合わせください。</p>
                <p>URL: </p><p>%s</p><p>誠にありがとうございます。</p><p>AuthenTrend、ATKey.loginより。</p>
            """;
        } else {
            return """
                <h3>親愛的 %s 您好，歡迎來到ATKey.login！</h3><p>請輸入以下驗證碼以繼續註冊您的ATKey.login帳戶：</p>
                <p>驗證碼：<strong>[%d]</strong></p><p>請注意，此驗證碼將在48小時內失效。</p>
                <p>如果您有任何問題，請聯繫我們的客戶服務團隊。</p>
                <p>網址：</p><p>%s</p><p>誠摯感謝您的支持，</p><p>AuthenTrend, ATKey.login</p>
            """;
        }
    }
}
