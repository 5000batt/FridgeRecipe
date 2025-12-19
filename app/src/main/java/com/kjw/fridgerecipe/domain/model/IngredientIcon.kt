package com.kjw.fridgerecipe.domain.model

enum class IngredientIcon(val id: String, val label: String, val category: CategoryType) {

    // 기타
    DEFAULT("default", "기본", CategoryType.ETC),
    KIMCHI("kimchi", "김치", CategoryType.ETC),
    TOFU("tofu", "두부", CategoryType.ETC),

    // 채소
    VEGETABLE("vegetable", "채소", CategoryType.VEGETABLE),
    CARROT("carrot", "당근", CategoryType.VEGETABLE),
    ONION("onion", "양파", CategoryType.VEGETABLE),
    POTATO("potato", "감자", CategoryType.VEGETABLE),
    SWEET_POTATO("sweet_potato", "고구마", CategoryType.VEGETABLE),
    TOMATO("tomato", "토마토", CategoryType.VEGETABLE),
    MUSHROOM("mushroom", "버섯", CategoryType.VEGETABLE),
    GARLIC("garlic", "마늘", CategoryType.VEGETABLE),
    GREEN_ONION("green_onion", "대파", CategoryType.VEGETABLE),
    RADISH("radish", "무", CategoryType.VEGETABLE),
    CABBAGE("cabbage", "배추", CategoryType.VEGETABLE),
    BEAN_SPROUTS("bean_sprouts", "콩나물", CategoryType.VEGETABLE),
    CHILI("chili", "고추", CategoryType.VEGETABLE),

    // 과일
    FRUIT("fruit", "과일", CategoryType.FRUIT),
    APPLE("apple", "사과", CategoryType.FRUIT),
    BANANA("banana", "바나나", CategoryType.FRUIT),
    LEMON("lemon", "레몬", CategoryType.FRUIT),
    TANGERINE("tangerine", "귤", CategoryType.FRUIT),
    GRAPE("grape", "포도", CategoryType.FRUIT),
    STRAWBERRY("strawberry", "딸기", CategoryType.FRUIT),

    // 육류
    MEAT("meat", "육류", CategoryType.MEAT),
    CHICKEN("chicken", "닭고기", CategoryType.MEAT),
    PORK("pork", "돼지고기", CategoryType.MEAT),
    BEEF("beef", "소고기", CategoryType.MEAT),
    DUCK("duck", "오리고기", CategoryType.MEAT),
    LAMB("lamb", "양고기", CategoryType.MEAT),

    // 해산물
    SEAFOOD("seafood", "해산물", CategoryType.SEAFOOD),
    FISH("fish", "생선", CategoryType.SEAFOOD),
    SHRIMP("shrimp", "새우", CategoryType.SEAFOOD),
    SQUID("squid", "오징어", CategoryType.SEAFOOD),
    CLAM("clam", "조개", CategoryType.SEAFOOD),
    CRAB("crab", "게", CategoryType.SEAFOOD),

    // 유제품/계란
    DAIRY("dairy", "유제품/계란", CategoryType.DAIRY),
    EGG("egg", "계란", CategoryType.DAIRY),
    MILK("milk", "우유", CategoryType.DAIRY),
    CHEESE("cheese", "치즈", CategoryType.DAIRY),
    YOGURT("yogurt", "요거트", CategoryType.DAIRY),

    // 곡물
    GRAIN("grain", "곡물", CategoryType.GRAIN),
    RICE("rice", "쌀", CategoryType.GRAIN),
    BREAD("bread", "빵", CategoryType.GRAIN),
    NOODLE("noodle", "면", CategoryType.GRAIN),

    // 소스/양념/조미료
    SEASONING("seasoning", "시즈닝", CategoryType.SEASONING),
    SALT("salt", "소금", CategoryType.SEASONING),
    SUGAR("sugar", "설탕", CategoryType.SEASONING),
    PEPPER("pepper", "후추", CategoryType.SEASONING),
    SOY_SAUCE("soy_sauce", "간장", CategoryType.SEASONING),
    DOENJANG("doenjang", "된장", CategoryType.SEASONING),
    GOCHUJANG("gochujang", "고추장", CategoryType.SEASONING),
    COOKING_OIL("cooking_oil", "식용유", CategoryType.SEASONING),
    SESAME_OIL("sesame_oil", "참기름", CategoryType.SEASONING),
    VINEGAR("vinegar", "식초", CategoryType.SEASONING),
    KETCHUP("ketchup", "케첩", CategoryType.SEASONING),
    MAYONNAISE("mayonnaise", "마요네즈", CategoryType.SEASONING),

    // 음료
    BEVERAGE("beverage", "음료", CategoryType.BEVERAGE),
    WATER("water", "물", CategoryType.BEVERAGE),
    SODA("soda", "탄산음료", CategoryType.BEVERAGE),
    BEER("beer", "맥주", CategoryType.BEVERAGE),
    SOJU("soju", "소주", CategoryType.BEVERAGE);

    companion object {
        fun fromString(label: String?): IngredientIcon {
            return IngredientIcon.entries.find { it.label == label } ?: DEFAULT
        }
    }
}