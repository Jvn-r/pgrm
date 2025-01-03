import java.util.*;

class Bank {
    String name;
    int netAmount = 0;
    Set<String> types = new HashSet<>();
}

public class CashFlowMinimizer {

    public static int getMinIndex(List<Bank> banks) {
        int minAmount = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < banks.size(); i++) {
            if (banks.get(i).netAmount < minAmount && banks.get(i).netAmount != 0) {
                minAmount = banks.get(i).netAmount;
                minIndex = i;
            }
        }
        return minIndex;
    }

    public static int getMaxIndex(List<Bank> banks) {
        int maxAmount = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < banks.size(); i++) {
            if (banks.get(i).netAmount > maxAmount) {
                maxAmount = banks.get(i).netAmount;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static Map.Entry<Integer, String> getMaxIndexWithMatchingType(
        List<Bank> banks, int minIndex, int maxNumTypes) {
        
        int maxAmount = Integer.MIN_VALUE, maxIndex = -1;
        String matchingType = null;

        for (int i = 0; i < banks.size(); i++) {
            if (banks.get(i).netAmount <= 0 || i == minIndex) continue;

            Set<String> intersection = new HashSet<>(banks.get(minIndex).types);
            intersection.retainAll(banks.get(i).types);

            if (!intersection.isEmpty() && banks.get(i).netAmount > maxAmount) {
                maxAmount = banks.get(i).netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }

        return new AbstractMap.SimpleEntry<>(maxIndex, matchingType);
    }

    public static void printTransactions(int numBanks, List<List<Map.Entry<Integer, String>>> ansGraph, List<Bank> banks) {
        System.out.println("\nThe transactions for minimizing cash flow are as follows:");
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (ansGraph.get(i).get(j).getKey() > 0) {
                    System.out.println(banks.get(i).name + " pays Rs " + ansGraph.get(i).get(j).getKey()
                            + " to " + banks.get(j).name + " via " + ansGraph.get(i).get(j).getValue() + ".");
                }
            }
        }
        System.out.println();
    }

    public static void minimizeCashFlow(int numBanks, List<Bank> banks, int[][] graph, int maxNumTypes) {
        List<Bank> netAmounts = new ArrayList<>(banks);

        for (int i = 0; i < numBanks; i++) {
            int incoming = 0, outgoing = 0;
            for (int j = 0; j < numBanks; j++) incoming += graph[j][i];
            for (int j = 0; j < numBanks; j++) outgoing += graph[i][j];
            netAmounts.get(i).netAmount = incoming - outgoing;
        }

        List<List<Map.Entry<Integer, String>>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            List<Map.Entry<Integer, String>> row = new ArrayList<>();
            for (int j = 0; j < numBanks; j++) row.add(new AbstractMap.SimpleEntry<>(0, ""));
            ansGraph.add(row);
        }

        int zeroNetAmounts = (int) netAmounts.stream().filter(b -> b.netAmount == 0).count();
        int matching = 0;

        while (zeroNetAmounts < numBanks) {
            int minIndex = getMinIndex(netAmounts);
            Map.Entry<Integer, String> maxAns = getMaxIndexWithMatchingType(netAmounts, minIndex, maxNumTypes);

            int maxIndex = maxAns.getKey();
            String matchingType = maxAns.getValue();
            
            if (maxIndex == -1 || matchingType == null) {
                System.out.println("\nNo compatible payment modes exist between " + netAmounts.get(minIndex).name + " and any creditor bank.");
                matching++;                
                break;
            }

            int transactionAmount = Math.min(
                Math.abs(netAmounts.get(minIndex).netAmount), netAmounts.get(maxIndex).netAmount);

            ansGraph.get(minIndex).set(maxIndex, new AbstractMap.SimpleEntry<>(transactionAmount, maxAns.getValue()));

            netAmounts.get(minIndex).netAmount += transactionAmount;
            netAmounts.get(maxIndex).netAmount -= transactionAmount;

            if (netAmounts.get(minIndex).netAmount == 0) zeroNetAmounts++;
            if (netAmounts.get(maxIndex).netAmount == 0) zeroNetAmounts++;
        }
        if (matching == 0)
            printTransactions(numBanks, ansGraph, banks);
        else 
            System.exit(0);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int numBanks;
        do {
            System.out.print("Enter the number of banks participating in the transactions: ");
            numBanks = sc.nextInt();
        } while (numBanks <= 0);

        List<Bank> banks = new ArrayList<>();
        System.out.println("Enter bank details (name, number of payment modes, and modes):");

        for (int i = 0; i < numBanks; i++) {
            Bank bank = new Bank();
            System.out.print("Bank " + (i + 1) + " details: ");
            bank.name = sc.next();
            int numModes = sc.nextInt();
            for (int j = 0; j < numModes; j++) {
                bank.types.add(sc.next());
            }
            banks.add(bank);
        }

        int numTransactions;
        do {
            System.out.print("Enter the number of transactions: ");
            numTransactions = sc.nextInt();
            if(numTransactions == 0){
                System.out.println("\nNo transactions made\n");
                System.exit(0);
            }
        } while (numTransactions <= 0);

        int[][] graph = new int[numBanks][numBanks];
        System.out.println("Enter transaction details (debtor, creditor, amount):");

        for (int i = 0; i < numTransactions; i++) {
            String debtor = sc.next(), creditor = sc.next();
            int amount = sc.nextInt();

            int debtorIndex = -1, creditorIndex = -1;
            for (int j = 0; j < numBanks; j++) {
                if (banks.get(j).name.equals(debtor)) debtorIndex = j;
                if (banks.get(j).name.equals(creditor)) creditorIndex = j;
            }

            if (debtorIndex != -1 && creditorIndex != -1) {
                graph[debtorIndex][creditorIndex] += amount;
            }
        }

        int maxNumTypes = banks.get(0).types.size();
        minimizeCashFlow(numBanks, banks, graph, maxNumTypes);

        sc.close();
    }
}
