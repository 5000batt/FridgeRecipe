package com.kjw.fridgerecipe.domain.model

enum class IngredientIcon(
    val id: String,
    val category: IngredientCategoryType,
) {
    // 기타
    DEFAULT("default", IngredientCategoryType.ETC),
    KIMCHI("kimchi", IngredientCategoryType.ETC),
    TOFU("tofu", IngredientCategoryType.ETC),

    // 채소
    VEGETABLE("vegetable", IngredientCategoryType.VEGETABLE),
    CARROT("carrot", IngredientCategoryType.VEGETABLE),
    ONION("onion", IngredientCategoryType.VEGETABLE),
    POTATO("potato", IngredientCategoryType.VEGETABLE),
    SWEET_POTATO("sweet_potato", IngredientCategoryType.VEGETABLE),
    TOMATO("tomato", IngredientCategoryType.VEGETABLE),
    MUSHROOM("mushroom", IngredientCategoryType.VEGETABLE),
    GARLIC("garlic", IngredientCategoryType.VEGETABLE),
    GREEN_ONION("green_onion", IngredientCategoryType.VEGETABLE),
    RADISH("radish", IngredientCategoryType.VEGETABLE),
    CABBAGE("cabbage", IngredientCategoryType.VEGETABLE),
    BEAN_SPROUTS("bean_sprouts", IngredientCategoryType.VEGETABLE),
    CHILI("chili", IngredientCategoryType.VEGETABLE),

    // 과일
    FRUIT("fruit", IngredientCategoryType.FRUIT),
    APPLE("apple", IngredientCategoryType.FRUIT),
    BANANA("banana", IngredientCategoryType.FRUIT),
    LEMON("lemon", IngredientCategoryType.FRUIT),
    TANGERINE("tangerine", IngredientCategoryType.FRUIT),
    GRAPE("grape", IngredientCategoryType.FRUIT),
    STRAWBERRY("strawberry", IngredientCategoryType.FRUIT),

    // 육류
    MEAT("meat", IngredientCategoryType.MEAT),
    CHICKEN("chicken", IngredientCategoryType.MEAT),
    PORK("pork", IngredientCategoryType.MEAT),
    BEEF("beef", IngredientCategoryType.MEAT),
    DUCK("duck", IngredientCategoryType.MEAT),
    LAMB("lamb", IngredientCategoryType.MEAT),

    // 해산물
    SEAFOOD("seafood", IngredientCategoryType.SEAFOOD),
    FISH("fish", IngredientCategoryType.SEAFOOD),
    SHRIMP("shrimp", IngredientCategoryType.SEAFOOD),
    SQUID("squid", IngredientCategoryType.SEAFOOD),
    CLAM("clam", IngredientCategoryType.SEAFOOD),
    CRAB("crab", IngredientCategoryType.SEAFOOD),

    // 유제품/계란
    DAIRY("dairy", IngredientCategoryType.DAIRY),
    EGG("egg", IngredientCategoryType.DAIRY),
    MILK("milk", IngredientCategoryType.DAIRY),
    CHEESE("cheese", IngredientCategoryType.DAIRY),
    YOGURT("yogurt", IngredientCategoryType.DAIRY),

    // 곡물
    GRAIN("grain", IngredientCategoryType.GRAIN),
    RICE("rice", IngredientCategoryType.GRAIN),
    BREAD("bread", IngredientCategoryType.GRAIN),
    NOODLE("noodle", IngredientCategoryType.GRAIN),

    // 소스/양념/조미료
    SEASONING("seasoning", IngredientCategoryType.SEASONING),
    SALT("salt", IngredientCategoryType.SEASONING),
    SUGAR("sugar", IngredientCategoryType.SEASONING),
    PEPPER("pepper", IngredientCategoryType.SEASONING),
    SOY_SAUCE("soy_sauce", IngredientCategoryType.SEASONING),
    DOENJANG("doenjang", IngredientCategoryType.SEASONING),
    GOCHUJANG("gochujang", IngredientCategoryType.SEASONING),
    COOKING_OIL("cooking_oil", IngredientCategoryType.SEASONING),
    SESAME_OIL("sesame_oil", IngredientCategoryType.SEASONING),
    VINEGAR("vinegar", IngredientCategoryType.SEASONING),
    KETCHUP("ketchup", IngredientCategoryType.SEASONING),
    MAYONNAISE("mayonnaise", IngredientCategoryType.SEASONING),

    // 음료
    BEVERAGE("beverage", IngredientCategoryType.BEVERAGE),
    WATER("water", IngredientCategoryType.BEVERAGE),
    SODA("soda", IngredientCategoryType.BEVERAGE),
    BEER("beer", IngredientCategoryType.BEVERAGE),
    SOJU("soju", IngredientCategoryType.BEVERAGE),
    ;

    companion object {
        fun fromId(id: String?): IngredientIcon = entries.find { it.id == id } ?: DEFAULT
    }
}
