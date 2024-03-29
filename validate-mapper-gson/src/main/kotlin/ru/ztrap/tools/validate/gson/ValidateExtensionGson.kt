package ru.ztrap.tools.validate.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import ru.ztrap.tools.validate.mapper.ValidateMapper
import ru.ztrap.tools.validate.mapper.findAnnotation

/**
 * @author Peter Gulko (zTrap)
 * @date 20.12.2019
 */
object ValidateExtensionGson {
    @JvmStatic fun install(builder: GsonBuilder.() -> Unit) {
        install(GsonBuilder().apply(builder).create())
    }

    @JvmStatic fun install(gson: Gson) {
        ValidateMapper.setNameExtractor {
            val mappedName = it.findAnnotation<SerializedName>()?.value
                ?: gson.fieldNamingStrategy().translateName(it)

            return@setNameExtractor "$mappedName (field: ${it.name})"
        }
    }

    @JvmStatic fun uninstall() {
        ValidateMapper.clearNameExtractor()
    }
}