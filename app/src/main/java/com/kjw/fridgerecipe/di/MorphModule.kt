package com.kjw.fridgerecipe.di

import com.kjw.fridgerecipe.data.util.UserDictionaryManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MorphModule {
    @Provides
    @Singleton
    fun provideKomoran(dictionaryManager: UserDictionaryManager): Komoran {
        // 코모란 인스턴스 생성 (모델 로딩 시작)
        val komoran = Komoran(DEFAULT_MODEL.LIGHT)

        // 이 시점에는 Remote Config가 아직 안 왔을 수 있으므로,
        // 이미 저장된 사전이 있다면 우선 적용합니다.
        dictionaryManager.getDictionaryPath()?.let {
            komoran.setUserDic(it)
        }

        return komoran
    }
}
