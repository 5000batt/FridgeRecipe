package com.kjw.fridgerecipe.domain.model

enum class IngredientIcon(val id: String, val label: String, val category: CategoryType) {

    // 기본
    DEFAULT("default", "기본", CategoryType.ETC),

    //채소
    CARROT("carrot", "당근", CategoryType.VEGETABLE),
    ONION("onion", "양파", CategoryType.VEGETABLE),
    POTATO("potato", "감자", CategoryType.VEGETABLE),
    TOMATO("tomato", "토마토", CategoryType.VEGETABLE),
    MUSHROOM("mushroom", "버섯", CategoryType.VEGETABLE),

    // 과일
    APPLE("apple", "사과", CategoryType.FRUIT),
    BANANA("banana", "바나나", CategoryType.FRUIT),
    LEMON("lemon", "레몬", CategoryType.FRUIT),

    // 육류
    MEAT("meat", "고기", CategoryType.MEAT),
    CHICKEN("chicken", "닭고기", CategoryType.MEAT),
    PORK("pork", "돼지고기", CategoryType.MEAT),
    BEEF("beef", "소고기", CategoryType.MEAT),

    // 해산물
    FISH("fish", "생선", CategoryType.SEAFOOD),
    SHRIMP("shrimp", "새우", CategoryType.SEAFOOD),

    // 유제품/계란
    EGG("egg", "계란", CategoryType.DAIRY),
    MILK("milk", "우유", CategoryType.DAIRY),
    CHEESE("cheese", "치즈", CategoryType.DAIRY),
    YOGURT("yogurt", "요거트", CategoryType.DAIRY),

    // 곡물/면/빵
    RICE("rice", "쌀/밥", CategoryType.GRAIN),
    BREAD("bread", "빵", CategoryType.GRAIN),
    NOODLE("noodle", "면", CategoryType.GRAIN),
    PASTA("pasta", "파스타", CategoryType.GRAIN),

    // 양념/오일
    SAUCE("sauce", "소스", CategoryType.SEASONING),
    OIL("oil", "오일", CategoryType.SEASONING),
    SPICE("spice", "양념", CategoryType.SEASONING),

    // 음료
    WATER("water", "물", CategoryType.BEVERAGE),
    BEER("beer", "맥주", CategoryType.BEVERAGE);

    companion object {
        fun fromString(label: String?): IngredientIcon {
            return IngredientIcon.entries.find { it.label == label } ?: DEFAULT
        }
    }
}