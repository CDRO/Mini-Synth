package ch.schmidlins.mini_synth.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.schmidlins.mini_synth.R
import java.io.File

class ProjectAdapter(
    private val projects: List<File>,
    private val onOpen: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_project_name)
        val btnOpen: Button = view.findViewById(R.id.btn_open_project)
        val btnDelete: Button = view.findViewById(R.id.btn_delete_project)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = projects[position]
        holder.tvName.text = project.name
        holder.btnOpen.setOnClickListener { onOpen(project) }
        holder.btnDelete.setOnClickListener { onDelete(project) }
    }

    override fun getItemCount() = projects.size
}
