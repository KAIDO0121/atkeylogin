package ATKeyLogin.backend.model;

public enum ShopifyOrderIdEnums {
    ONE_YEAR_LICENSE(8642088796444l, 46943961973020l, 1),
    TWO_YEARS_LICENSE(8642088796444l, 46943962005788l, 2),
    ONE_YEAR_RENEW(8642179858716l, 46943918457116l, 1),
    TWO_YEARS_RENEW(8642179858716l, 46943918489884l, 2);

    public final Long productId;
    public final Long variantId;
    public final int duration;



    ShopifyOrderIdEnums(Long productId, Long variantId, int duration) {
        this.productId = productId;
        this.variantId = variantId;
        this.duration = duration;
    }

}
