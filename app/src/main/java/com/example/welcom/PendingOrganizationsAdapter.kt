import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.welcom.R

class PendingOrganizationsAdapter(
    private val organizations: List<Organization>,
    private val onApproveClicked: (Organization) -> Unit
) : RecyclerView.Adapter<PendingOrganizationsAdapter.OrganizationViewHolder>() {

    inner class OrganizationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_organization, parent, false)
        return OrganizationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganizationViewHolder, position: Int) {
        val organization = organizations[position]
        holder.tvEmail.text = organization.email
        holder.tvDescription.text = organization.description

        // When the approve button is clicked, call the callback function
        holder.btnApprove.setOnClickListener {
            onApproveClicked(organization)
        }
    }

    override fun getItemCount() = organizations.size
}
