package com.surnft;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.2.
 */
@SuppressWarnings("rawtypes")
public class SurNFTCollection extends Contract {
  public static final String BINARY = "60806040523480156200001157600080fd5b506040516200232038038062002320833981016040819052620000349162000365565b848462000041336200010f565b60016200004f8382620004c5565b5060026200005e8282620004c5565b50505062000072836200015f60201b60201c565b6200007d82620001c3565b6001600160a01b038216600090815260046020526040812061ffff831690555b8161ffff168163ffffffff161015620001035760405163ffffffff8216906001600160a01b038516906000907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef908290a480620000fa8162000591565b9150506200009d565b505050505050620005c3565b600080546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b6200016962000242565b6000815111620001b15760405162461bcd60e51b815260206004820152600e60248201526d77726f6e6720626173652075726960901b60448201526064015b60405180910390fd5b600b620001bf8282620004c5565b5050565b620001cd62000242565b6001600160a01b038116620002345760405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608401620001a8565b6200023f816200010f565b50565b6000546001600160a01b031633146200029e5760405162461bcd60e51b815260206004820181905260248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152606401620001a8565b565b634e487b7160e01b600052604160045260246000fd5b600082601f830112620002c857600080fd5b81516001600160401b0380821115620002e557620002e5620002a0565b604051601f8301601f19908116603f01168101908282118183101715620003105762000310620002a0565b816040528381526020925086838588010111156200032d57600080fd5b600091505b8382101562000351578582018301518183018401529082019062000332565b600093810190920192909252949350505050565b600080600080600060a086880312156200037e57600080fd5b85516001600160401b03808211156200039657600080fd5b620003a489838a01620002b6565b96506020880151915080821115620003bb57600080fd5b620003c989838a01620002b6565b95506040880151915080821115620003e057600080fd5b50620003ef88828901620002b6565b606088015190945090506001600160a01b03811681146200040f57600080fd5b608087015190925061ffff811681146200042857600080fd5b809150509295509295909350565b600181811c908216806200044b57607f821691505b6020821081036200046c57634e487b7160e01b600052602260045260246000fd5b50919050565b601f821115620004c057600081815260208120601f850160051c810160208610156200049b5750805b601f850160051c820191505b81811015620004bc57828155600101620004a7565b5050505b505050565b81516001600160401b03811115620004e157620004e1620002a0565b620004f981620004f2845462000436565b8462000472565b602080601f831160018114620005315760008415620005185750858301515b600019600386901b1c1916600185901b178555620004bc565b600085815260208120601f198616915b82811015620005625788860151825594840194600190910190840162000541565b5085821015620005815787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b600063ffffffff808316818103620005b957634e487b7160e01b600052601160045260246000fd5b6001019392505050565b611d4d80620005d36000396000f3fe608060405234801561001057600080fd5b50600436106101425760003560e01c806355f804b3116100b857806395d89b411161007c57806395d89b411461029a578063a22cb465146102a2578063b88d4fde146102b5578063c87b56dd146102c8578063e985e9c5146102db578063f2fde38b1461031757600080fd5b806355f804b3146102485780636352211e1461025b57806370a082311461026e578063715018a6146102815780638da5cb5b1461028957600080fd5b806323b872dd1161010a57806323b872dd146101d65780632f745c59146101e957806340c10f19146101fc57806342842e0e1461020f57806342966c68146102225780634f6ccce71461023557600080fd5b806301ffc9a71461014757806306fdde031461016f578063081812fc14610184578063095ea7b3146101af57806318160ddd146101c4575b600080fd5b61015a610155366004611704565b61032a565b60405190151581526020015b60405180910390f35b610177610355565b6040516101669190611771565b610197610192366004611784565b6103e7565b6040516001600160a01b039091168152602001610166565b6101c26101bd3660046117b9565b61040e565b005b6009545b604051908152602001610166565b6101c26101e43660046117e3565b610528565b6101c86101f73660046117b9565b61055a565b6101c261020a3660046117b9565b6105f0565b6101c261021d3660046117e3565b610606565b6101c2610230366004611784565b610621565b6101c8610243366004611784565b610690565b6101c26102563660046118ab565b610723565b610197610269366004611784565b610779565b6101c861027c3660046118f4565b6107d8565b6101c261085e565b6000546001600160a01b0316610197565b610177610872565b6101c26102b036600461190f565b610881565b6101c26102c336600461194b565b61088c565b6101776102d6366004611784565b6108c4565b61015a6102e93660046119c7565b6001600160a01b03918216600090815260066020908152604080832093909416825291909152205460ff1690565b6101c26103253660046118f4565b61092b565b60006001600160e01b0319821663780e9d6360e01b148061034f575061034f826109a1565b92915050565b606060018054610364906119fa565b80601f0160208091040260200160405190810160405280929190818152602001828054610390906119fa565b80156103dd5780601f106103b2576101008083540402835291602001916103dd565b820191906000526020600020905b8154815290600101906020018083116103c057829003601f168201915b5050505050905090565b60006103f2826109f1565b506000908152600560205260409020546001600160a01b031690565b600061041982610779565b9050806001600160a01b0316836001600160a01b03160361048b5760405162461bcd60e51b815260206004820152602160248201527f4552433732313a20617070726f76616c20746f2063757272656e74206f776e656044820152603960f91b60648201526084015b60405180910390fd5b336001600160a01b03821614806104a757506104a781336102e9565b6105195760405162461bcd60e51b815260206004820152603d60248201527f4552433732313a20617070726f76652063616c6c6572206973206e6f7420746f60448201527f6b656e206f776e6572206f7220617070726f76656420666f7220616c6c0000006064820152608401610482565b6105238383610a41565b505050565b610533335b82610aaf565b61054f5760405162461bcd60e51b815260040161048290611a34565b610523838383610b2e565b6000610565836107d8565b82106105c75760405162461bcd60e51b815260206004820152602b60248201527f455243373231456e756d657261626c653a206f776e657220696e646578206f7560448201526a74206f6620626f756e647360a81b6064820152608401610482565b506001600160a01b03919091166000908152600760209081526040808320938352929052205490565b6105f8610c9f565b6106028282610cf9565b5050565b6105238383836040518060200160405280600081525061088c565b61062a3361052d565b6106845760405162461bcd60e51b815260206004820152602560248201527f6275726e2063616c6c6572206973206e6f74206f776e6572206e6f72206170706044820152641c9bdd995960da1b6064820152608401610482565b61068d81610d13565b50565b600061069b60095490565b82106106fe5760405162461bcd60e51b815260206004820152602c60248201527f455243373231456e756d657261626c653a20676c6f62616c20696e646578206f60448201526b7574206f6620626f756e647360a01b6064820152608401610482565b6009828154811061071157610711611a81565b90600052602060002001549050919050565b61072b610c9f565b600081511161076d5760405162461bcd60e51b815260206004820152600e60248201526d77726f6e6720626173652075726960901b6044820152606401610482565b600b6106028282611ae5565b60008061078583610db6565b90506001600160a01b03811661034f5760405162461bcd60e51b8152602060048201526018602482015277115490cdcc8c4e881a5b9d985b1a59081d1bdad95b88125160421b6044820152606401610482565b60006001600160a01b0382166108425760405162461bcd60e51b815260206004820152602960248201527f4552433732313a2061646472657373207a65726f206973206e6f7420612076616044820152683634b21037bbb732b960b91b6064820152608401610482565b506001600160a01b031660009081526004602052604090205490565b610866610c9f565b6108706000610dff565b565b606060028054610364906119fa565b610602338383610e4f565b6108963383610aaf565b6108b25760405162461bcd60e51b815260040161048290611a34565b6108be84848484610f1d565b50505050565b60606108cf826109f1565b60006108d9610f50565b905060008151116108f95760405180602001604052806000815250610924565b8061090384610f5f565b604051602001610914929190611ba5565b6040516020818303038152906040525b9392505050565b610933610c9f565b6001600160a01b0381166109985760405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608401610482565b61068d81610dff565b60006001600160e01b031982166380ac58cd60e01b14806109d257506001600160e01b03198216635b5e139f60e01b145b8061034f57506301ffc9a760e01b6001600160e01b031983161461034f565b6109fa81610ff2565b61068d5760405162461bcd60e51b8152602060048201526018602482015277115490cdcc8c4e881a5b9d985b1a59081d1bdad95b88125160421b6044820152606401610482565b600081815260056020526040902080546001600160a01b0319166001600160a01b0384169081179091558190610a7682610779565b6001600160a01b03167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92560405160405180910390a45050565b600080610abb83610779565b9050806001600160a01b0316846001600160a01b03161480610b0257506001600160a01b0380821660009081526006602090815260408083209388168352929052205460ff165b80610b265750836001600160a01b0316610b1b846103e7565b6001600160a01b0316145b949350505050565b826001600160a01b0316610b4182610779565b6001600160a01b031614610b675760405162461bcd60e51b815260040161048290611bd4565b6001600160a01b038216610bc95760405162461bcd60e51b8152602060048201526024808201527f4552433732313a207472616e7366657220746f20746865207a65726f206164646044820152637265737360e01b6064820152608401610482565b610bd6838383600161100f565b826001600160a01b0316610be982610779565b6001600160a01b031614610c0f5760405162461bcd60e51b815260040161048290611bd4565b600081815260056020908152604080832080546001600160a01b03199081169091556001600160a01b0387811680865260048552838620805460001901905590871680865283862080546001019055868652600390945282852080549092168417909155905184937fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef91a4505050565b6000546001600160a01b031633146108705760405162461bcd60e51b815260206004820181905260248201527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726044820152606401610482565b61060282826040518060200160405280600081525061114f565b6000610d1e82610779565b9050610d2e81600084600161100f565b610d3782610779565b600083815260056020908152604080832080546001600160a01b03199081169091556001600160a01b0385168085526004845282852080546000190190558785526003909352818420805490911690555192935084927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef908390a45050565b6000818152600360205260408120546001600160a01b0316610de3576000546001600160a01b031661034f565b506000908152600360205260409020546001600160a01b031690565b600080546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b816001600160a01b0316836001600160a01b031603610eb05760405162461bcd60e51b815260206004820152601960248201527f4552433732313a20617070726f766520746f2063616c6c6572000000000000006044820152606401610482565b6001600160a01b03838116600081815260066020908152604080832094871680845294825291829020805460ff191686151590811790915591519182527f17307eab39ab6107e8899845ad3d59bd9653f200f220920489ca2b5937696c31910160405180910390a3505050565b610f28848484610b2e565b610f3484848484611182565b6108be5760405162461bcd60e51b815260040161048290611c19565b6060600b8054610364906119fa565b60606000610f6c83611283565b600101905060008167ffffffffffffffff811115610f8c57610f8c61181f565b6040519080825280601f01601f191660200182016040528015610fb6576020820181803683370190505b5090508181016020015b600019016f181899199a1a9b1b9c1cb0b131b232b360811b600a86061a8153600a8504945084610fc057509392505050565b600080610ffe83610db6565b6001600160a01b0316141592915050565b61101b8484848461135b565b600181111561108a5760405162461bcd60e51b815260206004820152603560248201527f455243373231456e756d657261626c653a20636f6e7365637574697665207472604482015274185b9cd9995c9cc81b9bdd081cdd5c1c1bdc9d1959605a1b6064820152608401610482565b816001600160a01b0385166110e6576110e181600980546000838152600a60205260408120829055600182018355919091527f6e1540171b6c0c960b71a7020d9f60077f6af931a8bbf590da0223dacf75c7af0155565b611109565b836001600160a01b0316856001600160a01b0316146111095761110985826113e3565b6001600160a01b0384166111255761112081611480565b611148565b846001600160a01b0316846001600160a01b03161461114857611148848261152f565b5050505050565b6111598383611573565b6111666000848484611182565b6105235760405162461bcd60e51b815260040161048290611c19565b60006001600160a01b0384163b1561127857604051630a85bd0160e11b81526001600160a01b0385169063150b7a02906111c6903390899088908890600401611c6b565b6020604051808303816000875af1925050508015611201575060408051601f3d908101601f191682019092526111fe91810190611ca8565b60015b61125e573d80801561122f576040519150601f19603f3d011682016040523d82523d6000602084013e611234565b606091505b5080516000036112565760405162461bcd60e51b815260040161048290611c19565b805181602001fd5b6001600160e01b031916630a85bd0160e11b149050610b26565b506001949350505050565b60008072184f03e93ff9f4daa797ed6e38ed64bf6a1f0160401b83106112c25772184f03e93ff9f4daa797ed6e38ed64bf6a1f0160401b830492506040015b6d04ee2d6d415b85acef810000000083106112ee576d04ee2d6d415b85acef8100000000830492506020015b662386f26fc10000831061130c57662386f26fc10000830492506010015b6305f5e1008310611324576305f5e100830492506008015b612710831061133857612710830492506004015b6064831061134a576064830492506002015b600a831061034f5760010192915050565b60018111156108be576001600160a01b038416156113a1576001600160a01b0384166000908152600460205260408120805483929061139b908490611cdb565b90915550505b6001600160a01b038316156108be576001600160a01b038316600090815260046020526040812080548392906113d8908490611cee565b909155505050505050565b600060016113f0846107d8565b6113fa9190611cdb565b60008381526008602052604090205490915080821461144d576001600160a01b03841660009081526007602090815260408083208584528252808320548484528184208190558352600890915290208190555b5060009182526008602090815260408084208490556001600160a01b039094168352600781528383209183525290812055565b60095460009061149290600190611cdb565b6000838152600a6020526040812054600980549394509092849081106114ba576114ba611a81565b9060005260206000200154905080600983815481106114db576114db611a81565b6000918252602080832090910192909255828152600a9091526040808220849055858252812055600980548061151357611513611d01565b6001900381819060005260206000200160009055905550505050565b600061153a836107d8565b6001600160a01b039093166000908152600760209081526040808320868452825280832085905593825260089052919091209190915550565b6001600160a01b0382166115c95760405162461bcd60e51b815260206004820181905260248201527f4552433732313a206d696e7420746f20746865207a65726f20616464726573736044820152606401610482565b6115d281610ff2565b1561161f5760405162461bcd60e51b815260206004820152601c60248201527f4552433732313a20746f6b656e20616c7265616479206d696e746564000000006044820152606401610482565b61162d60008383600161100f565b61163681610ff2565b156116835760405162461bcd60e51b815260206004820152601c60248201527f4552433732313a20746f6b656e20616c7265616479206d696e746564000000006044820152606401610482565b6001600160a01b038216600081815260046020908152604080832080546001019055848352600390915280822080546001600160a01b0319168417905551839291907fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef908290a45050565b6001600160e01b03198116811461068d57600080fd5b60006020828403121561171657600080fd5b8135610924816116ee565b60005b8381101561173c578181015183820152602001611724565b50506000910152565b6000815180845261175d816020860160208601611721565b601f01601f19169290920160200192915050565b6020815260006109246020830184611745565b60006020828403121561179657600080fd5b5035919050565b80356001600160a01b03811681146117b457600080fd5b919050565b600080604083850312156117cc57600080fd5b6117d58361179d565b946020939093013593505050565b6000806000606084860312156117f857600080fd5b6118018461179d565b925061180f6020850161179d565b9150604084013590509250925092565b634e487b7160e01b600052604160045260246000fd5b600067ffffffffffffffff808411156118505761185061181f565b604051601f8501601f19908116603f011681019082821181831017156118785761187861181f565b8160405280935085815286868601111561189157600080fd5b858560208301376000602087830101525050509392505050565b6000602082840312156118bd57600080fd5b813567ffffffffffffffff8111156118d457600080fd5b8201601f810184136118e557600080fd5b610b2684823560208401611835565b60006020828403121561190657600080fd5b6109248261179d565b6000806040838503121561192257600080fd5b61192b8361179d565b91506020830135801515811461194057600080fd5b809150509250929050565b6000806000806080858703121561196157600080fd5b61196a8561179d565b93506119786020860161179d565b925060408501359150606085013567ffffffffffffffff81111561199b57600080fd5b8501601f810187136119ac57600080fd5b6119bb87823560208401611835565b91505092959194509250565b600080604083850312156119da57600080fd5b6119e38361179d565b91506119f16020840161179d565b90509250929050565b600181811c90821680611a0e57607f821691505b602082108103611a2e57634e487b7160e01b600052602260045260246000fd5b50919050565b6020808252602d908201527f4552433732313a2063616c6c6572206973206e6f7420746f6b656e206f776e6560408201526c1c881bdc88185c1c1c9bdd9959609a1b606082015260800190565b634e487b7160e01b600052603260045260246000fd5b601f82111561052357600081815260208120601f850160051c81016020861015611abe5750805b601f850160051c820191505b81811015611add57828155600101611aca565b505050505050565b815167ffffffffffffffff811115611aff57611aff61181f565b611b1381611b0d84546119fa565b84611a97565b602080601f831160018114611b485760008415611b305750858301515b600019600386901b1c1916600185901b178555611add565b600085815260208120601f198616915b82811015611b7757888601518255948401946001909101908401611b58565b5085821015611b955787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b60008351611bb7818460208801611721565b835190830190611bcb818360208801611721565b01949350505050565b60208082526025908201527f4552433732313a207472616e736665722066726f6d20696e636f72726563742060408201526437bbb732b960d91b606082015260800190565b60208082526032908201527f4552433732313a207472616e7366657220746f206e6f6e20455243373231526560408201527131b2b4bb32b91034b6b83632b6b2b73a32b960711b606082015260800190565b6001600160a01b0385811682528416602082015260408101839052608060608201819052600090611c9e90830184611745565b9695505050505050565b600060208284031215611cba57600080fd5b8151610924816116ee565b634e487b7160e01b600052601160045260246000fd5b8181038181111561034f5761034f611cc5565b8082018082111561034f5761034f611cc5565b634e487b7160e01b600052603160045260246000fdfea2646970667358221220722820cd77fc80e8e948148abaaa555be759922b626479b3ce862c8529c96a8b64736f6c63430008130033";

  public static final String FUNC_APPROVE = "approve";

  public static final String FUNC_BALANCEOF = "balanceOf";

  public static final String FUNC_BURN = "burn";

  public static final String FUNC_GETAPPROVED = "getApproved";

  public static final String FUNC_ISAPPROVEDFORALL = "isApprovedForAll";

  public static final String FUNC_MINT = "mint";

  public static final String FUNC_NAME = "name";

  public static final String FUNC_OWNER = "owner";

  public static final String FUNC_OWNEROF = "ownerOf";

  public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

  public static final String FUNC_safeTransferFrom = "safeTransferFrom";

  public static final String FUNC_SETAPPROVALFORALL = "setApprovalForAll";

  public static final String FUNC_SETBASEURI = "setBaseURI";

  public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

  public static final String FUNC_SYMBOL = "symbol";

  public static final String FUNC_TOKENBYINDEX = "tokenByIndex";

  public static final String FUNC_TOKENOFOWNERBYINDEX = "tokenOfOwnerByIndex";

  public static final String FUNC_TOKENURI = "tokenURI";

  public static final String FUNC_TOTALSUPPLY = "totalSupply";

  public static final String FUNC_TRANSFERFROM = "transferFrom";

  public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

  public static final Event APPROVAL_EVENT = new Event("Approval",
      Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
  ;

  public static final Event APPROVALFORALL_EVENT = new Event("ApprovalForAll",
      Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}));
  ;

  public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
      Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
  ;

  public static final Event TRANSFER_EVENT = new Event("Transfer",
      Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));
  ;

  @Deprecated
  protected SurNFTCollection(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected SurNFTCollection(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected SurNFTCollection(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected SurNFTCollection(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
    ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      ApprovalEventResponse typedResponse = new ApprovalEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
    return web3j.ethLogFlowable(filter).map(new Function<Log, ApprovalEventResponse>() {
      @Override
      public ApprovalEventResponse apply(Log log) {
        Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.approved = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
      }
    });
  }

  public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
    return approvalEventFlowable(filter);
  }

  public static List<ApprovalForAllEventResponse> getApprovalForAllEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVALFORALL_EVENT, transactionReceipt);
    ArrayList<ApprovalForAllEventResponse> responses = new ArrayList<ApprovalForAllEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(EthFilter filter) {
    return web3j.ethLogFlowable(filter).map(new Function<Log, ApprovalForAllEventResponse>() {
      @Override
      public ApprovalForAllEventResponse apply(Log log) {
        Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(APPROVALFORALL_EVENT, log);
        ApprovalForAllEventResponse typedResponse = new ApprovalForAllEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.operator = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.approved = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
      }
    });
  }

  public Flowable<ApprovalForAllEventResponse> approvalForAllEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(APPROVALFORALL_EVENT));
    return approvalForAllEventFlowable(filter);
  }

  public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
    ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
    return web3j.ethLogFlowable(filter).map(new Function<Log, OwnershipTransferredEventResponse>() {
      @Override
      public OwnershipTransferredEventResponse apply(Log log) {
        Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
        return typedResponse;
      }
    });
  }

  public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
    return ownershipTransferredEventFlowable(filter);
  }

  public static List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
    ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      TransferEventResponse typedResponse = new TransferEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
    return web3j.ethLogFlowable(filter).map(new Function<Log, TransferEventResponse>() {
      @Override
      public TransferEventResponse apply(Log log) {
        Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.tokenId = (BigInteger) eventValues.getIndexedValues().get(2).getValue();
        return typedResponse;
      }
    });
  }

  public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
    return transferEventFlowable(filter);
  }

  public RemoteFunctionCall<TransactionReceipt> approve(String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_APPROVE,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to),
            new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<BigInteger> balanceOf(String owner) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_BALANCEOF,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<TransactionReceipt> burn(BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_BURN,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<String> getApproved(BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETAPPROVED,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<Boolean> isApprovedForAll(String owner, String operator) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISAPPROVEDFORALL,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner),
            new org.web3j.abi.datatypes.Address(160, operator)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<TransactionReceipt> mint(String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_MINT,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, to),
            new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<String> name() {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_NAME,
        Arrays.<Type>asList(),
        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<String> owner() {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER,
        Arrays.<Type>asList(),
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<String> ownerOf(BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNEROF,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_RENOUNCEOWNERSHIP,
        Arrays.<Type>asList(),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_safeTransferFrom,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
            new org.web3j.abi.datatypes.Address(160, to),
            new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> safeTransferFrom(String from, String to, BigInteger tokenId, byte[] data) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_safeTransferFrom,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
            new org.web3j.abi.datatypes.Address(160, to),
            new org.web3j.abi.datatypes.generated.Uint256(tokenId),
            new org.web3j.abi.datatypes.DynamicBytes(data)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setApprovalForAll(String operator, Boolean approved) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_SETAPPROVALFORALL,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, operator),
            new org.web3j.abi.datatypes.Bool(approved)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> setBaseURI(String buri) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_SETBASEURI,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(buri)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SUPPORTSINTERFACE,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
    return executeRemoteCallSingleValueReturn(function, Boolean.class);
  }

  public RemoteFunctionCall<String> symbol() {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SYMBOL,
        Arrays.<Type>asList(),
        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<BigInteger> tokenByIndex(BigInteger index) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOKENBYINDEX,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(index)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<BigInteger> tokenOfOwnerByIndex(String owner, BigInteger index) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOKENOFOWNERBYINDEX,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, owner),
            new org.web3j.abi.datatypes.generated.Uint256(index)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<String> tokenURI(BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOKENURI,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteFunctionCall<BigInteger> totalSupply() {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TOTALSUPPLY,
        Arrays.<Type>asList(),
        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteFunctionCall<TransactionReceipt> transferFrom(String from, String to, BigInteger tokenId) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_TRANSFERFROM,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, from),
            new org.web3j.abi.datatypes.Address(160, to),
            new org.web3j.abi.datatypes.generated.Uint256(tokenId)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
    final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
        FUNC_TRANSFEROWNERSHIP,
        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)),
        Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  @Deprecated
  public static SurNFTCollection load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    return new SurNFTCollection(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static SurNFTCollection load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    return new SurNFTCollection(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static SurNFTCollection load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
    return new SurNFTCollection(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static SurNFTCollection load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
    return new SurNFTCollection(contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static RemoteCall<SurNFTCollection> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String name, String symbol, String uri, String owner, BigInteger count) {
    String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
        new org.web3j.abi.datatypes.Utf8String(symbol),
        new org.web3j.abi.datatypes.Utf8String(uri),
        new org.web3j.abi.datatypes.Address(160, owner),
        new org.web3j.abi.datatypes.generated.Uint16(count)));
    return deployRemoteCall(SurNFTCollection.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
  }

  public static RemoteCall<SurNFTCollection> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String name, String symbol, String uri, String owner, BigInteger count) {
    String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
        new org.web3j.abi.datatypes.Utf8String(symbol),
        new org.web3j.abi.datatypes.Utf8String(uri),
        new org.web3j.abi.datatypes.Address(160, owner),
        new org.web3j.abi.datatypes.generated.Uint16(count)));
    return deployRemoteCall(SurNFTCollection.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
  }

  @Deprecated
  public static RemoteCall<SurNFTCollection> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String name, String symbol, String uri, String owner, BigInteger count) {
    String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
        new org.web3j.abi.datatypes.Utf8String(symbol),
        new org.web3j.abi.datatypes.Utf8String(uri),
        new org.web3j.abi.datatypes.Address(160, owner),
        new org.web3j.abi.datatypes.generated.Uint16(count)));
    return deployRemoteCall(SurNFTCollection.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
  }

  @Deprecated
  public static RemoteCall<SurNFTCollection> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String name, String symbol, String uri, String owner, BigInteger count) {
    String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(name),
        new org.web3j.abi.datatypes.Utf8String(symbol),
        new org.web3j.abi.datatypes.Utf8String(uri),
        new org.web3j.abi.datatypes.Address(160, owner),
        new org.web3j.abi.datatypes.generated.Uint16(count)));
    return deployRemoteCall(SurNFTCollection.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
  }

  public static class ApprovalEventResponse extends BaseEventResponse {
    public String owner;

    public String approved;

    public BigInteger tokenId;
  }

  public static class ApprovalForAllEventResponse extends BaseEventResponse {
    public String owner;

    public String operator;

    public Boolean approved;
  }

  public static class OwnershipTransferredEventResponse extends BaseEventResponse {
    public String previousOwner;

    public String newOwner;
  }

  public static class TransferEventResponse extends BaseEventResponse {
    public String from;

    public String to;

    public BigInteger tokenId;
  }
}
