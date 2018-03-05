## BTCP Full Node Wallet

Tests to perform before each release

1. Connection
	1. User can connect for the first time. This includes an empty BTCPrivate dir
	2. User can connect consecutively. This includes being able to resume from previous sync

2. Key management
	1. User can import T private key
	2. User can import Z private key
	3. User can export T private key
	4. User can export Z private key
	5. User can Sweep after importing either T or Z private key
		1. When the private key has no (confirmed) balance, the user is informed

3. Transactions
	1. User can send from T->T
	2. User can send from T->Z
	3. User can send from Z->T
	4. User can send from Z->Z
	5. Coinbase transaction error handling is correctly done ('e.g. needs to send full amount to Z').

4. Messaging
	1. User can create messaging identity
	2. User can export messaging identity
	3. User can import contacts
	4. User can send messages
	5. User can receive messages