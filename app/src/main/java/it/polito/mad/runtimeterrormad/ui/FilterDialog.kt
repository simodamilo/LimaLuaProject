package it.polito.mad.runtimeterrormad.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.runtimeterrormad.R
import it.polito.mad.runtimeterrormad.viewmodels.OnSaleListViewModel
import kotlinx.android.synthetic.main.fragment_filter.*
import java.time.LocalDate
import java.util.*


class FilterDialog : DialogFragment() {
    private val globalItemListVM: OnSaleListViewModel by activityViewModels()
    private lateinit var dialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.apply_some_filter))
            .setPositiveButton(resources.getString(R.string.findButton)) { _, _ ->
                if (resources.getStringArray(R.array.categories)
                        .indexOf(categoryFilterDialogAT.text.toString()) != -1
                    || expiryDateFilterDialogET.text.toString() != ""
                    || minPriceFilterDialogET.text.toString() != ""
                    || maxPriceFilterDialogET.text.toString() != ""
                ) {
                    globalItemListVM.categoryID = resources.getStringArray(R.array.categories)
                        .indexOf(categoryFilterDialogAT.text.toString())
                    globalItemListVM.date = expiryDateFilterDialogET.text.toString()
                    globalItemListVM.minPrice = minPriceFilterDialogET.text.toString()
                    globalItemListVM.maxPrice = maxPriceFilterDialogET.text.toString()
                    globalItemListVM.setFilters()
                    val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
                    toolbar?.menu?.findItem(R.id.filterOnSaleMI)?.setIcon(R.drawable.ic_filter)
                }
                dismiss()
            }
            .setNegativeButton(resources.getString(R.string.clearFiltersButton)) { it, _ ->
                globalItemListVM.categoryID = -1
                globalItemListVM.date = ""
                globalItemListVM.minPrice = ""
                globalItemListVM.maxPrice = ""
                globalItemListVM.setFilters()
                val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
                toolbar?.menu?.findItem(R.id.filterOnSaleMI)?.setIcon(R.drawable.ic_no_filter)
                it.cancel()
            }.create()
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)
        dialog.setView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (globalItemListVM.categoryID != -1)
            categoryFilterDialogAT.setText(resources.getStringArray(R.array.categories)[globalItemListVM.categoryID])
        expiryDateFilterDialogET.setText(globalItemListVM.date)
        minPriceFilterDialogET.setText(globalItemListVM.minPrice)
        maxPriceFilterDialogET.setText(globalItemListVM.maxPrice)

        this.context?.let {
            val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.categories)
            ) {

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val itemView: TextView = super.getDropDownView(
                        position,
                        convertView,
                        parent
                    ) as TextView

                    if (position == 0 || position == 5 || position == 10
                        || position == 16 || position == 22 || position == 27
                        || position == 40 || position == 49
                    ) {
                        itemView.setPaddingRelative(32, 0, 32, 0)
                        itemView.background = ColorDrawable(Color.parseColor("#F5F5F5"))
                        itemView.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        itemView.setPaddingRelative(128, 0, 32, 0)
                        itemView.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                        itemView.typeface = Typeface.DEFAULT
                    }

                    return itemView
                }

                override fun isEnabled(position: Int): Boolean {
                    return position != 0 && position != 5 && position != 10
                            && position != 16 && position != 22 && position != 27
                            && position != 40 && position != 49
                }
            }
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryFilterDialogAT.setAdapter(adapter)
        }

        expiryDateFilterDialogET.inputType = InputType.TYPE_NULL
        expiryDateFilterDialogET.setOnClickListener {
            var year: Int
            var month: Int
            var day: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                LocalDate.now().let { date ->
                    day = date.dayOfMonth
                    month = date.monthValue
                    year = date.year
                }
            else {
                Calendar.getInstance().let { date ->
                    day = date.get(Calendar.DAY_OF_MONTH)
                    month = date.get(Calendar.MONTH)
                    year = date.get(Calendar.YEAR)
                }
            }
            DatePickerDialog(
                it.context,
                DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                    expiryDateFilterDialogET.setText(
                        String.format(
                            "%02d-%02d-%04d",
                            mDay,
                            mMonth + 1,
                            mYear
                        )
                    )
                }, year, month - 1, day
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1
            }.show()
        }
    }


}