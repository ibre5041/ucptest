# TCP keepavlie howto for Oracle
# (Ivan Brezina@dhl.com)
#

TCP Keepalive:
http://tldp.org/HOWTO/TCP-Keepalive-HOWTO/overview.html

TCP Keeplive is used for
 - Checking for dead peers
 - Preventing disconnection due to network inactivity

When it is turned on the kernel sends packets having no data payload over the connection in predefined interval.
The default values are:


# cat /proc/sys/net/ipv4/tcp_keepalive_time
7200
Delay after the 1st probe is sent - 2 hours. 
NOTE: the default value for CISCO ASA's "connection timeout idle" is just 1 hour!!!

# cat /proc/sys/net/ipv4/tcp_keepalive_intvl
75
Interval between probes.

# cat /proc/sys/net/ipv4/tcp_keepalive_probes
9
The number of "lost" probes indicating that the connection is dead.

Oracle:
TCP Keepalive is not active for oracle connections by default. It can be turned on by setting (ENABLE=BROKEN) under DESCRIPTION element.

Test:
Connect using:

(DESCRIPTION=
    (ENABLE=BROKEN)
    (TRANSPORT_CONNECT_TIMEOUT=90)
    (RETRY_COUNT=20)
    (FAILOVER=ON)
    (ADDRESS=(PROTOCOL=TCP)(HOST=rac-cluster-scan)(PORT=1521))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=ACTEST))
)

[~]# nslookup rac-cluster-scan 
Server:         127.0.0.1
Address:        127.0.0.1#53

Name:   rac-cluster-scan.prod.vmware.haf
Address: 192.168.8.209

[~]# ip route get 192.168.8.209
192.168.8.209 dev ens224 src 192.168.8.200
    cache

[~]# tcpdump -pni ens224 -v "tcp port 1521 and ( tcp[tcpflags] & tcp-ack != 0 and ( (ip[2:2] - ((ip[0]&0xf)<<2) ) - ((tcp[12]&0xf0)>>2) ) == 0 )"
tcpdump: listening on ens224, link-type EN10MB (Ethernet), capture size 262144 bytes
14:56:49.994675 IP (tos 0x0, ttl 64, id 12096, offset 0, flags [DF], proto TCP (6), length 52)
    192.168.8.200.37196 > 192.168.8.212.ncube-lm: Flags [.], cksum 0x9313 (incorrect -> 0x4b94), ack 2263270845, win 359, options [nop,nop,TS val 78140944 ecr 7133273], length 0
14:56:49.994829 IP (tos 0x0, ttl 64, id 55327, offset 0, flags [DF], proto TCP (6), length 52)
    192.168.8.212.ncube-lm > 192.168.8.200.37196: Flags [.], cksum 0xadcf (correct), ack 1, win 207, options [nop,nop,TS val 7138281 ecr 78110940], length 0
14:56:55.002668 IP (tos 0x0, ttl 64, id 12097, offset 0, flags [DF], proto TCP (6), length 52)
    192.168.8.200.37196 > 192.168.8.212.ncube-lm: Flags [.], cksum 0x9313 (incorrect -> 0x2474), ack 1, win 359, options [nop,nop,TS val 78145952 ecr 7138281], length 0
14:56:55.002809 IP (tos 0x0, ttl 64, id 55328, offset 0, flags [DF], proto TCP (6), length 52)
    192.168.8.212.ncube-lm > 192.168.8.200.37196: Flags [.], cksum 0x9a3f (correct), ack 1, win 207, options [nop,nop,TS val 7143289 ecr 78110940], length 0
14:57:00.010680 IP (tos 0x0, ttl 64, id 12098, offset 0, flags [DF], proto TCP (6), length 52)
    192.168.8.200.37196 > 192.168.8.212.ncube-lm: Flags [.], cksum 0x9313 (incorrect -> 0xfd53), ack 1, win 359, options [nop,nop,TS val 78150960 ecr 7143289], length 0
14:57:00.011296 IP (tos 0x0, ttl 64, id 55329, offset 0, flags [DF], proto TCP (6), length 52)
    192.168.8.212.ncube-lm > 192.168.8.200.37196: Flags [.], cksum 0x86af (correct), ack 1, win 207, options [nop,nop,TS val 7148297 ecr 78110940], length 0

6 packets captured
6 packets received by filter
0 packets dropped by kernel
