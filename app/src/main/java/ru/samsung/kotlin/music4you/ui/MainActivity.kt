package ru.samsung.kotlin.music4you.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import ru.samsung.kotlin.music4you.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var musicFragment: MusicFragment
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MusicViewModel::class.java)

        val adapter = ViewPagerAdapter(this)
        musicFragment = MusicFragment()
        adapter.addFragment(musicFragment, "Музыка")
        adapter.addFragment(PlayerFragment(), "Плеер")
        adapter.addFragment(AboutFragment(), "О нас")

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Установка начальной вкладки на "Музыка"
        binding.viewPager.currentItem = 0

        // Обработка текста в поисковой строке
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            musicFragment.searchMusic(text.toString())
        }
    }
}
