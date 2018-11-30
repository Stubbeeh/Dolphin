import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Class PortfolioGenerator
 * Generates all needed portfolios from a pool of assets.
 * Only generates seemingly most promising portfolios, based on some criteria:
 *  - Sharpe Scope : All assets must have there sharpe over a certain minimum
 *  - Return : as long as in sharpe scope, best return wins
 *
 *  To speed up the process, we use categories of assets.
 *  Assets with a near covariance are regrouped in a same class.
 *  Then when creating the portfolio, for each asset it wants to add, it
 *  searches for its best counterpart by iterating on the category that was
 *  classified as the one with the farthest covariance.
 *  This allows to decrease the portfolio volatility, increasing its sharpe,
 *  without having to test each and every combination of assets.
 */

public class PortfoliosGenerator {
    private ArrayList<AssetCategory> categories_;

    public PortfoliosGenerator(ArrayList<AssetCategory> categories) {
        categories_ = categories;
    }

    public ArrayList<ArrayList<Asset>> generate() {

        List<Asset> assets = /* FIXME: Get all assets by Sharpe */;
        HashSet<Integer> portfolio_ass = new HashSet<>();
        ArrayList<ArrayList<Asset>> portfolios = new ArrayList<>();
        double average_sharpe = /* sharpe moyen */;
        double sharpe_scope = /* sharpe moyen actifs dont sharpe > X */;
        double pace = (sharpe_scope - average_sharpe) / 20;

        /*
         * Get all assets by Sharpe.
         * Get best Sharpe.
         * Look for best counterpart category.
         *
         * Then, window-based algo:
         * As long as in SHARPE_SCOPE -> best profit wins.
         * Can slide SHARPE_SCOPE to create other portfolios.
         * Just determine what minimum SHARPE_SCOPE we can attain.
         * Generate all portfolios to that minimum.
         * Could opti here by removing same trees, but could wait for tree explo too....
         * */

        while (sharpe_scope > average_sharpe) {
            ArrayList<Asset> portfolio = new ArrayList<>();
            int nb_assets_port = 0;
            int root_nb = 0;

            while (nb_assets_port < 20
                    && root_nb < assets.size()
                    && assets.get(root_nb).sharpe_ >= sharpe_scope) {

                while(portfolio_ass.contains(assets.get(root_nb).id_))
                    ++root_nb;

                Asset cur_ass = assets.get(root_nb);

                portfolio.add(cur_ass);
                portfolio_ass.add(cur_ass.id_);
                ++nb_assets_port;

                int cp_nb = 0;

                while(cp_nb < cur_ass.cat_get().counterparts_.size()
                        && cur_ass.equals(assets.get(root_nb))) {
                    AssetCategory counterpart = cur_ass.cat_get().counterparts_.get(cp_nb).getValue();
                    Asset best = null;

                    for (int i = 0; i < counterpart.assets_.size(); i++)
                        if (counterpart.get(i).sharpe_ >= sharpe_scope)
                            if (best == null || best.return_ < counterpart.get(i).return_)
                                best = counterpart.get(i);

                    if (best != null)
                        cur_ass = best;
                    else
                        ++cp_nb;
                }

                if (cur_ass.equals(assets.get(root_nb)))
                    return null;

                portfolio.add(cur_ass);
                portfolio_ass.add(cur_ass.id_);
                ++nb_assets_port;
            }
            if (nb_assets_port == 20)
                portfolios.add(portfolio);
            sharpe_scope -= pace;
        }

        return portfolios;
    }
}
