FROM openjdk:8-jdk-stretch as builder
ARG SBT_VERSION=1.2.8
ARG BDM_VERSION="latest"
ARG BRANCH="version-0.17.x"
ARG DEB_PACKAGE_NETWORKS

RUN                                                                             \
{                                                                               \
    BDM_VERSION=$BDM_VERSION                                            &&  \
    test $BDM_VERSION = "latest"                                          &&  \
    BDM_VERSION=$(curl -fsSL                                                  \
        https://api.github.com/repos/bdmplatform/Bdm/releases/latest        \
        | tac | grep -m 1 'tag_name.:' | tr -cd '[0-9\.]')                  &&  \
    echo "Using latest version '${BDM_VERSION}'"                              \
;} ;                                                                            \
{                                                                               \
    curl -fsSL https://api.github.com/repos/bdmplatform/Bdm/releases        \
        | tac | grep -q "tag_name.*${BDM_VERSION}"                        &&  \
    echo "GitHub release '${BDM_VERSION}' found"                              \
;} &&                                                                           \
{                                                                               \
    echo "Downloading Bdm '${BDM_VERSION}' from GitHub"                 &&  \
    mkdir -p /bdm/node/target                                             &&  \
    releaseUrl="https://github.com/bdmplatform/Bdm/releases/download"   &&  \
    rawContentUrl="https://raw.githubusercontent.com/bdmplatform/Bdm"   &&  \
                                                                                \
    echo "Downloading jar file"                                             &&  \
    curl -fL ${releaseUrl}/v${BDM_VERSION}/bdm-all-${BDM_VERSION}.jar     \
        -o /bdm/node/target/bdm-all-${BDM_VERSION}.jar                &&  \
                                                                                \
    echo "Downloading mainnet config"                                       &&  \
    curl -fL ${rawContentUrl}/v${BDM_VERSION}/node/bdm-mainnet.conf         \
        -o /bdm/node/bdm-mainnet.conf                                   &&  \
                                                                                \
    echo "Downloading testnet config"                                       &&  \
    curl -fL ${rawContentUrl}/v${BDM_VERSION}/node/bdm-testnet.conf         \
        -o /bdm/node/bdm-testnet.conf                                   &&  \
    echo "Downloading stagenet config"                                      &&  \
    curl -fL ${rawContentUrl}/v${BDM_VERSION}/node/bdm-stagenet.conf        \
        -o /bdm/node/bdm-stagenet.conf                                  ||  \
            curl -fL ${rawContentUrl}/master/node/bdm-stagenet.conf           \
            -o /bdm/node/bdm-stagenet.conf                                  \
;} ||                                                                           \
{                                                                               \
    echo "Downloading sbt '${SBT_VERSION}'"                                 &&  \
    curl -fL -o sbt-$SBT_VERSION.deb                                            \
        https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb              &&  \
    dpkg -i sbt-$SBT_VERSION.deb && rm sbt-$SBT_VERSION.deb                 &&  \
    git clone https://github.com/bdmplatform/Bdm.git                        \
        --branch $BRANCH bdm && cd bdm                                  &&  \
    git config --global user.name "Sbt Builder"                             &&  \
    git config --global user.email "sbt@builder.docker"                     &&  \
    git tag -a "v${BDM_VERSION}" -m "Docker build"                        &&  \
    SBT_OPTS="-Xmx2g -XX:MaxPermSize=256m -XX:ReservedCodeCacheSize=128m"       \
    sbt "node/assembly"                                                     &&  \
    for network in $DEB_PACKAGE_NETWORKS ; do                                   \
        echo "Building '${network}' package"                                &&  \
        SBT_OPTS="-XX:ReservedCodeCacheSize=128m                                \
            -Xmx2g -Dnetwork=${network}" sbt 'packageAll'                   &&  \
        mkdir -p /out/${network}                                            &&  \
        mv node/target/bdm*.deb /out/${network}/                          &&  \
        cp node/target/bdm-all*.jar /out/${network}/                      &&  \
        mv grpc-server/target/universal/grpc-server*.tgz /out/${network}/   &&  \
        mv grpc-server/target/grpc-server*.deb /out/${network}/             ;   \
    done                                                                        \
;}

FROM openjdk:11-jre-slim
ARG BDM_VERSION="latest"
ARG BDM_LOG_LEVEL="DEBUG"
ARG BDM_HEAP_SIZE="2g"
ARG BDM_NETWORK="mainnet"

ENV BDM_VERSION=$BDM_VERSION
ENV BDM_LOG_LEVEL=$BDM_LOG_LEVEL
ENV BDM_HEAP_SIZE=$BDM_HEAP_SIZE
ENV BDM_NETWORK=$BDM_NETWORK

COPY --from=builder /bdm/node/target/bdm-all-*.jar /usr/share/bdm/lib/
COPY --from=builder /bdm/node/bdm-*.conf /usr/share/bdm/conf/
COPY entrypoint.sh /usr/share/bdm/bin/

RUN groupadd -g 143 bdm                                                   &&  \
    useradd -d /var/lib/bdm -g 143 -u 143 -s /bin/bash -M bdm           &&  \
    mkdir -p /var/lib/bdm /etc/bdm                                      &&  \
    chown -R 143:143 /var/lib/bdm /usr/share/bdm /etc/bdm             &&  \
    chmod -R 755 /var/lib/bdm /usr/share/bdm /etc/bdm                 &&  \
    ln -fs /var/lib/bdm/log /var/log/bdm

WORKDIR /var/lib/bdm
EXPOSE 6869 6868 6863 6862

USER bdm
VOLUME /var/lib/bdm
ENTRYPOINT ["/usr/share/bdm/bin/entrypoint.sh"]
