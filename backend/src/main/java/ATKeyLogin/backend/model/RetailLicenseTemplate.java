package ATKeyLogin.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RetailLicenseTemplate {

    private String language;

    public String getSubject() {
        
        if (this.language.equals("zh-Hant")) {
            return "請立即啟用您的授權碼並開始體驗！";
        } else if (this.language.equals("ja")) {
            return "今すぐライセンスをアクティベートして、開始してください！";
        } else {
            return "Please activate your license right now and get started!";
        }
    }

    public String getBody() {
        if (this.language.equals("zh-Hant")) {
            return """
                <h3>親愛的 %s 您好，歡迎來到ATKey.Login！</h3><p>這是您的ATKey.Login授權碼： <strong>[%s]</strong></p>
                <p>請至<a href=\"%s\">ATKey.Login管理頁面</a>啟用授權碼，並下載安裝客戶端應用程式。</p>
                <p>使用應用程式完成相關設置後，即可開始體驗ATKey.Login無密碼驗證登入個人電腦！</p>
                <p></p>
                <p>請注意您的7天試用版將自動轉換為付費版本，您將可以享有無斷點的ATKey.Login產品體驗。</p>
                <p>如果您有任何問題，請聯繫我們的客戶服務團隊。</p>
                <p></p>
                <p>誠摯感謝您的支持，</p>
                <p>AuthenTrend, ATKey.login</p>
            """;
        } else if (this.language.equals("ja")) {
            return """
                <h3>%s 様、こんにちは。ATKey.loginへようこそ！</h3><p>ATKey.Loginのライセンスコードです: <strong>[%s]</strong></p>
                <p><a href=\"%s\">ATKey.Login ウェブサイト</a>から、コンパニオンツールをアクティベートし、インストールしてください。</p>
                <p>一度コンパニオンツールでセットアップすれば、ATKeyでパスワードレスログインを体験できる！</p>
                <p></p>
                <p>7日間のトライアルは自動的に有料版に切り替わります、ATKey.Loginを引き続きお楽しみいただけます。</p>
                <p>ご質問がありましたら、お気軽に当社のカスタマーサポートまでご連絡ください。</p>
                <p></p>
                <p>よろしくお願いいたします。</p>
                <p>AuthenTrend、ATKey.loginより。</p>
            """;
        } else {
            return """
                <h3>Hi %s, Welcome to ATKey.login!</h3><p>This is your license code for ATKey.Login: <strong>[%s]</strong></p>
                <p>Please visit <a href=\"%s\">ATKey.Login website</a> to activate and then install the companion tool.</p>
                <p>Once you have finished setup with the companion tool, you can enjoy</p>
                <p>passwordless login with ATKey! </p>
                <p></p>
                <p>Please contact our customer support team if you have any questions.</p>
                <p></p>
                <p>Sincerely, </p>
                <p>AuthenTrend, ATKey.login</p>
            """;
        }
    }
}
