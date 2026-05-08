package com.rom.poa_firstapp.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseModule {
    private const val SUPABASE_URL = "https://tthltglhujmekpbfebxd.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_Kb5-m2fWH6GM672QG_qPkg_RGNkp_9G"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        httpEngine = OkHttp.create()
        install(Postgrest)
        install(Realtime)
        install(io.github.jan.supabase.auth.Auth)
        install(Storage)
    }
}
