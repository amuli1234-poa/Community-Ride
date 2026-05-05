package com.rom.poa_firstapp.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseModule {
    private const val SUPABASE_URL = "https://tthltglhujmekpbfebxd.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_Kb5-m2fWH6GM672QG_qPkg_RGNkp_9G"

    val client = createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
        install(Postgrest)
        install(Realtime)
        install(io.github.jan.supabase.auth.Auth)
    }
}
