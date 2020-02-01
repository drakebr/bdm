#!/bin/bash
NETWORKS="mainnet testnet stagenet"

mkdir -p /var/lib/bdm/log
if [ ! -f /etc/bdm/bdm.conf ] ; then
	echo "Custom '/etc/bdm/bdm.conf' not found. Using a default one for '${BDM_NETWORK,,}' network." | tee -a /var/log/bdm/bdm.log
	if [[  $NETWORKS == *"${BDM_NETWORK,,}"* ]] ; then
		cp /usr/share/bdm/conf/bdm-${BDM_NETWORK}.conf /etc/bdm/bdm.conf
		# filtering default api-key-hash. remove the string below once 'node/bdm-testnet.conf'is updated in the github repo
		sed -i 's/api-key-hash = "H6nsiifwYKYEx6YzYD7woP1XCn72RVvx6tC1zjjLXqsu"//' /etc/bdm/bdm.conf
	else
		echo "Network '${BDM_NETWORK,,}' not found. Exiting."
		exit 1
	fi
else
	echo "Found custom '/etc/bdm/bdm.conf'. Using it."
fi


if [ "${BDM_VERSION}" == "latest" ] ; then
	filename=$(find /usr/share/bdm/lib -name bdm-all* -printf '%f\n')
	export BDM_VERSION=$(echo ${filename##*-} | cut -d\. -f1-3)
fi

[ -n "${BDM_WALLET_PASSWORD}" ] && JAVA_OPTS="${JAVA_OPTS} -Dbdm.wallet.password=${BDM_WALLET_PASSWORD}"
[ -n "${BDM_WALLET_SEED}" ] && JAVA_OPTS="${JAVA_OPTS} -Dbdm.wallet.seed=${BDM_WALLET_SEED}"

JAVA_OPTS="${JAVA_OPTS} -Dbdm.data-directory=/var/lib/bdm/data -Dbdm.directory=/var/lib/bdm"

echo "Node is starting..." | tee -a /var/log/bdm/bdm.log
echo "BDM_HEAP_SIZE='${BDM_HEAP_SIZE}'" | tee -a /var/log/bdm/bdm.log
echo "BDM_LOG_LEVEL='${BDM_LOG_LEVEL}'" | tee -a /var/log/bdm/bdm.log
echo "BDM_VERSION='${BDM_VERSION}'" | tee -a /var/log/bdm/bdm.log
echo "BDM_NETWORK='${BDM_NETWORK}'" | tee -a /var/log/bdm/bdm.log
echo "BDM_WALLET_SEED='${BDM_WALLET_SEED}'" | tee -a /var/log/bdm/bdm.log
echo "BDM_WALLET_PASSWORD='${BDM_WALLET_PASSWORD}'" | tee -a /var/log/bdm/bdm.log
echo "JAVA_OPTS='${JAVA_OPTS}'" | tee -a /var/log/bdm/bdm.log

java -Dlogback.stdout.level=${BDM_LOG_LEVEL} \
	-XX:+ExitOnOutOfMemoryError \
	-Xmx${BDM_HEAP_SIZE} \
	-Dlogback.file.directory=/var/log/bdm \
	-Dconfig.override_with_env_vars=true \
	${JAVA_OPTS} \
	-jar /usr/share/bdm/lib/bdm-all-${BDM_VERSION}.jar /etc/bdm/bdm.conf

