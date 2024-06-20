package uv.tc.tesisapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import uv.tc.tesisapp.adapter.VideoAdapter
import uv.tc.tesisapp.pojo.Video
import java.io.File

class ActividadesFragment : Fragment() {

    private lateinit var adapter: VideoAdapter
    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_actividades, container, false)
        recycler = rootView.findViewById(R.id.recycler_videos)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseApp.initializeApp(requireContext())

        val arrayList = ArrayList<Video>()
        adapter = VideoAdapter(requireContext(), arrayList)

        adapter.setOnItemClickListener(object : VideoAdapter.OnItemClickListener {
            override fun onClick(video: Video) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
                intent.setDataAndType(Uri.parse(video.url), "video/mp4")
                startActivity(intent)
            }
        })

        recycler.adapter = adapter

        FirebaseStorage.getInstance().reference.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { storageReference ->
                    val video = Video()
                    val fileName = storageReference.name
                    video.title = fileName.substring(0, fileName.lastIndexOf('.'))
                    storageReference.downloadUrl.addOnCompleteListener { task ->
                        val url = "https://" + task.result?.encodedAuthority + task.result?.encodedPath +
                                "?alt=media&token=" + task.result?.getQueryParameters("token")?.get(0)
                        video.url = url
                        arrayList.add(video)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar los videos", Toast.LENGTH_SHORT).show()
            }

    }
}




