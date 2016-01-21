# turbokrist
The first-ever GPU accelerated Krist miner - using JavaCL. The original project, [JCLMiner](https://github.com/apemanzilla/JCLMiner), was abandoned in favor of this rewrite for various reasons.

## Building

First, clone the repository. Make sure to include the `--recursive` option.

`git clone --recursive https://github.com/apemanzilla/turbokrist.git`

Enter the cloned repository.

`cd turbokrist`

Build the code. Omit the `./` on Windows.

`./gradlew build`

If the code was built without errors, you can find command-line archives containing the start scripts and libraries in `turbokrist-cli/build/distributions`
