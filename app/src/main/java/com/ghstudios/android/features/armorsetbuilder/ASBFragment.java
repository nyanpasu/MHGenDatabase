package com.ghstudios.android.features.armorsetbuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import com.ghstudios.android.data.classes.ASBSession;
import com.ghstudios.android.data.classes.ASBTalisman;
import com.ghstudios.android.data.classes.Decoration;
import com.ghstudios.android.data.classes.SkillTree;
import com.ghstudios.android.data.database.DataManager;
import com.ghstudios.android.features.armor.ArmorDetailPagerActivity;
import com.ghstudios.android.features.decorations.DecorationDetailPagerActivity;
import com.ghstudios.android.mhgendatabase.R;
import com.ghstudios.android.MHUtils;
import com.ghstudios.android.features.armor.ArmorListPagerActivity;

/**
 * This is where the magic happens baby. Users can define a custom armor set in this fragment.
 */
public class ASBFragment extends Fragment implements ASBPagerActivity.OnASBSetActivityUpdateListener {

    public static final String ARG_SET_RANK = "set_rank";
    public static final String ARG_SET_HUNTER_TYPE = "set_hunter_type";

    ASBSession session;

    private ASBPieceContainer[] equipmentViews = new ASBPieceContainer[6];

    public static ASBFragment newInstance(int setRank, int setHunterType) {
        Bundle args = new Bundle();
        ASBFragment f = new ASBFragment();
        args.putInt(ARG_SET_RANK, setRank);
        args.putInt(ARG_SET_HUNTER_TYPE, setHunterType);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asb, container, false);

        equipmentViews[0] = (ASBPieceContainer) view.findViewById(R.id.armor_builder_head);
        equipmentViews[1] = (ASBPieceContainer) view.findViewById(R.id.armor_builder_body);
        equipmentViews[2] = (ASBPieceContainer) view.findViewById(R.id.armor_builder_arms);
        equipmentViews[3] = (ASBPieceContainer) view.findViewById(R.id.armor_builder_waist);
        equipmentViews[4] = (ASBPieceContainer) view.findViewById(R.id.armor_builder_legs);
        equipmentViews[5] = (ASBPieceContainer) view.findViewById(R.id.armor_builder_talisman);

        equipmentViews[0].initialize(session, 0, this);
        equipmentViews[1].initialize(session, 1, this);
        equipmentViews[2].initialize(session, 2, this);
        equipmentViews[3].initialize(session, 3, this);
        equipmentViews[4].initialize(session, 4, this);
        equipmentViews[5].initialize(session, 5, this);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) { // If the user canceled the request, we don't want to do anything.
            switch (requestCode) {
                case ASBPagerActivity.REQUEST_CODE_ADD_PIECE:
                    new ASBAsyncTask(ASBOperation.ADD_PIECE, data).execute();
                    break;

                case ASBPagerActivity.REQUEST_CODE_ADD_DECORATION:
                    new ASBAsyncTask(ASBOperation.ADD_DECORATION, data).execute();
                    break;

                case ASBPagerActivity.REQUEST_CODE_CREATE_TALISMAN:
                    new ASBAsyncTask(ASBOperation.CREATE_TALISMAN, data).execute();
                    break;

                case ASBPagerActivity.REQUEST_CODE_REMOVE_PIECE:
                    new ASBAsyncTask(ASBOperation.REMOVE_PIECE, data).execute();
                    break;

                case ASBPagerActivity.REQUEST_CODE_REMOVE_DECORATION:
                    new ASBAsyncTask(ASBOperation.REMOVE_DECORATION, data).execute();
                    break;
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_asb, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_builder_add_piece: // The user wants to add an armor piece
                Intent intent = new Intent(getActivity(), ArmorListPagerActivity.class);
                intent.putExtra(ASBPagerActivity.EXTRA_FROM_SET_BUILDER, true);

                startActivityForResult(intent, ASBPagerActivity.REQUEST_CODE_ADD_PIECE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        ASBPagerActivity a = (ASBPagerActivity) getActivity();
        a.addASBSetChangedListener(this);
        session = a.getASBSession();
    }

    @Override
    public void onASBActivityUpdated(ASBSession s) {
        for (ASBPieceContainer c : equipmentViews) {
            c.updateContents();
        }
    }

    /** Called when the user clicks the drop-down arrow on an equipment view. */
    public void onDecorationsMenuOpened() {
        for (ASBPieceContainer c : equipmentViews) {
            c.hideDecorations();
        }
    }

    private enum ASBOperation {
        ADD_PIECE {
            @Override
            public void perform(ASBSession session, Context context, Intent data) {
                long armorId = data.getLongExtra(ArmorDetailPagerActivity.EXTRA_ARMOR_ID, -1);

                String armorType = DataManager.get(context).getArmor(armorId).getSlot();

                switch (armorType) {
                    case "Head":
                        session.setEquipment(ASBSession.HEAD, DataManager.get(context).getArmor(armorId));
                        DataManager.get(context).queryPutASBSessionArmor(session.getId(), armorId, ASBSession.HEAD);
                        break;
                    case "Body":
                        session.setEquipment(ASBSession.BODY, DataManager.get(context).getArmor(armorId));
                        DataManager.get(context).queryPutASBSessionArmor(session.getId(), armorId, ASBSession.BODY);
                        break;
                    case "Arms":
                        session.setEquipment(ASBSession.ARMS, DataManager.get(context).getArmor(armorId));
                        DataManager.get(context).queryPutASBSessionArmor(session.getId(), armorId, ASBSession.ARMS);
                        break;
                    case "Waist":
                        session.setEquipment(ASBSession.WAIST, DataManager.get(context).getArmor(armorId));
                        DataManager.get(context).queryPutASBSessionArmor(session.getId(), armorId, ASBSession.WAIST);
                        break;
                    case "Legs":
                        session.setEquipment(ASBSession.LEGS, DataManager.get(context).getArmor(armorId));
                        DataManager.get(context).queryPutASBSessionArmor(session.getId(), armorId, ASBSession.LEGS);
                        break;
                }
            }
        },

        ADD_DECORATION {
            @Override
            void perform(ASBSession session, Context context, Intent data) {
                long decorationId = data.getLongExtra(DecorationDetailPagerActivity.EXTRA_DECORATION_ID, -1);
                int pieceIndex = data.getIntExtra(ASBPagerActivity.EXTRA_PIECE_INDEX, -1);

                Decoration decoration = DataManager.get(context).getDecoration(decorationId);
                int decorationIndex = session.addDecoration(pieceIndex, decoration);

                if (decorationIndex != -1 && pieceIndex != -1) {
                    DataManager.get(context).queryPutASBSessionDecoration(session.getId(), decorationId, pieceIndex, decorationIndex);
                }
            }
        },

        CREATE_TALISMAN {
            @Override
            void perform(ASBSession session, Context context, Intent data) {
                ASBTalisman talisman;

                int typeIndex = data.getIntExtra(ASBPagerActivity.EXTRA_TALISMAN_TYPE_INDEX, -1);
                int slots = data.getIntExtra(ASBPagerActivity.EXTRA_TALISMAN_SLOTS, 0);

                long skill1Id = data.getLongExtra(ASBPagerActivity.EXTRA_TALISMAN_SKILL_TREE_1, -1);
                int skill1Points = data.getIntExtra(ASBPagerActivity.EXTRA_TALISMAN_SKILL_POINTS_1, -1);

                long skill2Id = -1;
                int skill2Points = 0;

                SkillTree skill1Tree = DataManager.get(context).getSkillTree(skill1Id);
                talisman = new ASBTalisman(skill1Tree, skill1Points, typeIndex);
                talisman.setName(MHUtils.splitStringInArrayByComma(R.array.talisman_names, typeIndex, 0, context) + " Talisman");
                talisman.setNumSlots(slots);

                if (data.hasExtra(ASBPagerActivity.EXTRA_TALISMAN_SKILL_TREE_2)) {
                    skill2Id = data.getLongExtra(ASBPagerActivity.EXTRA_TALISMAN_SKILL_TREE_2, -1);
                    skill2Points = data.getIntExtra(ASBPagerActivity.EXTRA_TALISMAN_SKILL_POINTS_2, -1);

                    SkillTree skill2Tree = DataManager.get(context).getSkillTree(skill2Id);
                    talisman.setSkill2(skill2Tree);
                    talisman.setSkill2Points(skill2Points);
                }

                DataManager.get(context).queryCreateASBSessionTalisman(session.getId(), typeIndex, slots, skill1Id, skill1Points, skill2Id, skill2Points);

                session.setEquipment(ASBSession.TALISMAN, talisman);
            }
        },

        REMOVE_PIECE {
            @Override
            void perform(ASBSession session, Context context, Intent data) {
                int pieceIndex = data.getIntExtra(ASBPagerActivity.EXTRA_PIECE_INDEX, -1);
                session.removeEquipment(pieceIndex);

                if (pieceIndex == ASBSession.TALISMAN) {
                    DataManager.get(context).queryRemoveASBSessionTalisman(session.getId());
                } else {
                    DataManager.get(context).queryRemoveASBSessionArmor(session.getId(), pieceIndex);
                }
            }
        },

        REMOVE_DECORATION {
            @Override
            void perform(ASBSession session, Context context, Intent data) {
                int pieceIndex = data.getIntExtra(ASBPagerActivity.EXTRA_PIECE_INDEX, -1);
                int decorationIndex = data.getIntExtra(ASBPagerActivity.EXTRA_DECORATION_INDEX, -1);

                session.removeDecoration(pieceIndex, decorationIndex);
                DataManager.get(context).queryRemoveASBSessionDecoration(session.getId(), pieceIndex, decorationIndex);
            }
        };

        abstract void perform(ASBSession session, Context context, Intent data);
    }

    private class ASBAsyncTask extends AsyncTask<Void, Void, Void> {

        private ASBOperation operation;
        private Intent data;

        public ASBAsyncTask(ASBOperation operation, Intent data) {
            super();
            this.operation = operation;
            this.data = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
            operation.perform(session, getActivity(), data);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ((ASBPagerActivity) getActivity()).updateASBSetChangedListeners();
        }
    }
}