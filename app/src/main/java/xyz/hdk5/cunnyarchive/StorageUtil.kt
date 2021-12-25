package xyz.hdk5.cunnyarchive

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import com.google.common.io.ByteStreams
import java.io.File

const val MX_PACKAGE_NAME = "com.nexon.bluearchive"
const val MX_DATA_PATH = "Android/data/$MX_PACKAGE_NAME"
const val MX_DOCID = "primary:$MX_DATA_PATH"
const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
val MX_URI = DocumentsContract.buildDocumentUri(
    EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
    MX_DOCID,
)
val MX_TREE_URI = DocumentsContract.buildTreeDocumentUri(
    EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
    MX_DOCID,
)

@Suppress("ClassName")
object makeMxTreeUri {

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun invokeQ(path: String): Uri {
        val docid = "$MX_DOCID/$path"
        val uri = DocumentsContract.buildDocumentUriUsingTree(
            MX_TREE_URI,
            docid,
        )
        return uri
    }

    private fun invokeLegacy(path: String): Uri {
        @Suppress("DEPRECATION")
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        val storagePath = externalStorageDirectory.absolutePath
        return Uri.fromFile(File("$storagePath/$MX_DATA_PATH/$path"))
    }

    operator fun invoke(path: String): Uri {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> ::invokeQ
            else -> ::invokeLegacy
        }(path)
    }
}

fun delete(context: Context, uri: Uri): Boolean {
    val contentResolver = context.contentResolver
    return when (uri.scheme) {
        "file" -> File(uri.path!!).delete()
        "content" -> DocumentsContract.deleteDocument(contentResolver, uri)
        else -> false
    }
}

fun copy(context: Context, src: Uri, dst: Uri): Boolean {
    val contentResolver = context.contentResolver

    return contentResolver.openInputStream(src).use { inputStream ->
        contentResolver.openOutputStream(dst, "wt").use { outputStream ->
            ByteStreams.copy(inputStream!!, outputStream!!) > 0
        }
    }
}
