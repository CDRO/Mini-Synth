package ch.schmidlins.mini_synth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import ch.schmidlins.mini_synth.audio.SynthManager
import ch.schmidlins.mini_synth.databinding.FragmentProjectBrowserBinding
import java.io.File

class ProjectBrowserFragment(
    private val synthManager: SynthManager,
    private val onProjectLoaded: () -> Unit
) : DialogFragment() {

    private var _binding: FragmentProjectBrowserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProjectBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.rvProjects.layoutManager = LinearLayoutManager(context)
        refreshList()

        binding.btnNewProject.setOnClickListener {
            showNewProjectDialog()
        }

        binding.btnCloseBrowser.setOnClickListener {
            dismiss()
        }
    }

    private fun refreshList() {
        val projectDir = File(context?.filesDir, "projects")
        if (!projectDir.exists()) projectDir.mkdirs()
        
        val projects = projectDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
        binding.rvProjects.adapter = ProjectAdapter(projects, { project ->
            synthManager.loadProject(project.absolutePath)
            onProjectLoaded()
            dismiss()
        }, { project ->
            showDeleteConfirm(project)
        })
    }

    private fun showNewProjectDialog() {
        val input = EditText(context)
        input.hint = "Project Name"
        AlertDialog.Builder(requireContext())
            .setTitle("New Project")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val dir = File(File(context?.filesDir, "projects"), name)
                    if (dir.exists()) {
                        AlertDialog.Builder(requireContext()).setMessage("Project exists").show()
                    } else {
                        dir.mkdirs()
                        synthManager.saveProject(dir.absolutePath)
                        refreshList()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirm(project: File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete ${project.name}?")
            .setMessage("All samples and patterns will be lost.")
            .setPositiveButton("Delete") { _, _ ->
                project.deleteRecursively()
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
