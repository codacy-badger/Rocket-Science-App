package ru.chernakov.feature_app_bubblegame.presentation.menu

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import kotlinx.android.synthetic.main.fragment_bubble_game_menu.*
import org.koin.android.viewmodel.ext.android.viewModel
import ru.chernakov.core_ui.presentation.fragment.BaseFragment
import ru.chernakov.feature_app_bubblegame.R
import ru.chernakov.feature_app_bubblegame.navigation.OnBackPressedListener
import ru.chernakov.feature_app_bubblegame.presentation.host.BubbleGameHostFragment
import ru.chernakov.feature_app_bubblegame.presentation.host.BubbleGameViewModel
import ru.chernakov.feature_app_bubblegame.presentation.menu.settings.BubbleGameSettingsDialog
import ru.chernakov.feature_app_bubblegame.presentation.menu.settings.GameSettingsModel
import ru.chernakov.feature_app_bubblegame.presentation.menu.settings.GameSettingsOnClickListener
import ru.chernakov.feature_app_bubblegame.presentation.widget.BubbleGameStateListener

class BubbleGameMenuFragment : BaseFragment(), GameSettingsOnClickListener {
    private val bubbleGameViewModel: BubbleGameViewModel by viewModel()

    private lateinit var onBackPressedListener: OnBackPressedListener
    private lateinit var gameStateListener: BubbleGameStateListener

    private var gameSettings = GameSettingsModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btStartGame.setOnClickListener {
            bubbleGameViewModel.gameInteractor.bubbleCount = gameSettings.bubbleCount
            bubbleGameViewModel.gameInteractor.speed = gameSettings.gameSpeed
            bubbleGameViewModel.gameInteractor.gameTime = gameSettings.gameTime
            gameStateListener.onSettingsSet()
        }
        btSettings.setOnClickListener {
            BubbleGameSettingsDialog.show(childFragmentManager, gameSettings)
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressedListener.onMenuBackPressed()
                }
            })
    }

    override fun onApply(gameSettingsModel: GameSettingsModel) {
        this.gameSettings = gameSettingsModel
    }

    override fun getLayout() = R.layout.fragment_bubble_game_menu

    override fun obtainViewModel() = bubbleGameViewModel

    companion object {
        fun newInstance(hostFragment: BubbleGameHostFragment): BubbleGameMenuFragment {
            return BubbleGameMenuFragment().apply {
                gameStateListener = hostFragment
                onBackPressedListener = hostFragment
            }
        }
    }
}