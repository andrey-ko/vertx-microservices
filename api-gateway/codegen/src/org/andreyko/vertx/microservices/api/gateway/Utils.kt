package org.andreyko.vertx.microservices.api.gateway

import com.satori.libs.common.kotlin.*
import java.io.*

fun ICodeGenEnv.writeJavaClass(className: String, block: (Writer) -> Unit) {
  val pckgDir = cfg.pckg.replace(".", "/")
  file("${cfg.out}/$pckgDir/${className}.java") {
    write { os ->
      os.writer().use { ow ->
        block(ow)
      }
    }
    println("file generated '${path.fileName}' ('${path}')")
  }
}
