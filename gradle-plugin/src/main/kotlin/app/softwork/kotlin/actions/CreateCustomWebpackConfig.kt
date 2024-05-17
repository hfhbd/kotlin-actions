package app.softwork.kotlin.actions

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Input
import java.io.*

@CacheableTask
abstract class CreateCustomWebpackConfig : DefaultTask() {
    @get:Input
    abstract val entryFileName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun write() {
        val file = File(outputDir.asFile.get(), "webpack.kotlin.actions.node.js")

        file.writeText(
            //language=javascript
            """
// https://github.com/sindresorhus/builtin-modules
const builtInNodeModules = [
    "assert",
	"assert/strict",
	"async_hooks",
	"buffer",
	"child_process",
	"cluster",
	"console",
	"constants",
	"crypto",
	"dgram",
	"diagnostics_channel",
	"dns",
	"dns/promises",
	"domain",
	"events",
	"fs",
	"fs/promises",
	"http",
	"http2",
	"https",
	"inspector",
	"inspector/promises",
	"module",
	"net",
	"os",
	"path",
	"path/posix",
	"path/win32",
	"perf_hooks",
	"process",
	"punycode",
	"querystring",
	"readline",
	"readline/promises",
	"repl",
	"stream",
	"stream/consumers",
	"stream/promises",
	"stream/web",
	"string_decoder",
	"timers",
	"timers/promises",
	"tls",
	"trace_events",
	"tty",
	"url",
	"util",
	"util/types",
	"v8",
	"vm",
	"wasi",
	"worker_threads",
	"zlib"
];

            config.experiments = {
              outputModule: true,
            };
            config.devtool = false;
            
            config.target = 'es2022';

config.externals = [
    async function ({request}) {
        const isBuiltIn = request.startsWith('node:')
            || builtInNodeModules.includes(request);

        if (isBuiltIn) {
            return Promise.resolve(`module ${'$'}{request}`);
        }
    }
]
config.resolve = {
    conditionNames: ['import', 'node']
};

            config.output = {
              filename: '${entryFileName.get()}',
              path: config.output.path,
              
              libraryTarget: 'module',
              library: {
                type: "module",
              },
              module: true,
              chunkFormat: 'module',
              chunkLoading: 'import',
              environment: {
                module: true,
              }
            };

        """.trimIndent()
        )
    }
}
